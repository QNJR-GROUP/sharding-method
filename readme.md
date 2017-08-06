# 核心特性
全数据库全SQL兼容、完美RR级别读写分离、与原生一致的ACID特性、轻量简单易扩展

# 另外一个轮子的意义
很多人会质疑 市面上较为流行的Sharding中间件/应用层Sharing框架已经有很多，他们都已经发展了很久了，功能也很强大，为什么要一个再重复制造这么个轮子呢？

之所以这里有一个新的轮子并不是因为我懒得无事，而是我对目前基于传统RDB上Sharding框架的设计的理念不太赞同，虽然他们或许都很圆，跑的很快，但是使用不当的话，容易翻车。我期望的轮子是既能跑的快，但也能跑的稳。


我们目前国内主流的Sharding框架都是基于SQL来完成，其主要流程：
1. 是解析上层传入的SQL
2. 结合对应的分表分库配置，对传入的SQL进行改写并分发到对应的单机数据库上
3. 获得各个单机数据库的返回结果后，根据原SQL归并结果，返回用户期待的结果

这种实现希望提供一个屏蔽底层Sharding逻辑的解决方案，对上层应用来说，只有一个RDB,这样应用可以透明访问多个数据库。

然而，这仅仅只是一个美丽的目标。因种种原因，这些层次的Sharding方案都无法提供跟原生数据库一样的功能：
* ACID里的A无法保证
* ACID里的C可能被打破
* ACID里的I与原生不一致
* 由于SQL解析复杂，性能等考虑，很多数据库SQL不支持

正因为存在这些差异，本质上，上层应用必须明确的知道经过此类Sharding方案后得到的查询结果、事务结果与原生的有啥不一致才能写出正确可靠的程序。

**因此，基于SQL的Sharding方案对应用层并不透明。**

如果要基于SQL层的框架写出正确可靠的代码的话，我们需要遵循一些范式：
* 所有事务（包括读、写）都不能跨库
* 跨分片的查询提供的隔离级别与原生不一致
* 某些聚合查询的性能消耗很大，要慎用
* ......

这些范式对于实际上就是使用Sharding数据库框架时，不透明的表现。而这些表现都是隐式的隐藏于SQL中，难以REVIEW。

而且这些范式对于很多人来说不一定能够充分理解执行的含义，以至于忽略了。

由上面最重要的一点“所有事务（包括读、写）都不能跨库”决定，**一个合理设计的代码里绝大多数的业务代码中数据库访问都不会跨分区，核心业务代码都在同一分区内进行。**
因此，我们大多数情况下，需要的只是一个协助我们便捷选择对应分片的一个框架。

因此我的想法很简单，提供一个方便透明选择分片、并辅以自动生成ID的框架。对于需要访问多个分片的少数业务，框架提供手段，便捷地获取所有分片数据库的数据，并由用户自行归并得出所需结果（简单的归并框架可以自动进行）。

# 基本使用方法

先简略展示以下框架的基本用法（以下代码在UT案例中，但为突出重点，有所裁剪）

Service层

	@Service
	@ShardingContext(dataSourceSet="orderSet",shardingKeyEls="[user].userId",shardingStrategy="@modUserId",generateIdStrategy="@snowflaker",generateIdEls="[user].userId")
	public class UserServceImpl {
		
		@Autowired
		private UserDaoImpl userDao;
		
		@Transactional
		@SelectDataSource
		public void updateUser(User user){
			userDao.updateUser(user);
		}
		
		@Transactional
		@SelectDataSource
		@GenerateId
		public void saveUser(User user){
			userDao.saveUser(user);
		}
		
		@Transactional(readOnly=true)
		@SelectDataSource(keyNameEls="[userId]")
		public User findUser(int userId){
			return userDao.findUser(userId);
		}
		
		public List<User> findAllUsers(){
			return userDao.findAllUsers();
		}
		
    	public double calcUserAvgAge(){
    		List<User> allUsers = userDao.findAllUsers();
    		return allUsers.stream().mapToInt(u->u.getAge()).average().getAsDouble();
    	}
	}

@ShardingContext表示当前的Service的Sharding上下文，就是说，如果有 选择数据源、Map到各数据库Reduce出结果、生成ID等操作时，如果某些参数没有指定，都从这个ShardingContext里面的配置取

@SelectDataSource表示为该方法内执行的SQL根据Sharding策略选择一个Sharding数据源，在方法结束返回前，不能更改Sharding数据源

@GenerateId表示生成ID，并将其赋值到参数的指定位置

@GenerateId对应的逻辑会先执行，然后到@SelectDataSource然后到@Transaction

@Transactional(readOnly=true)标签指定了事务时只读的，因此框架会根据readOnly标志自动选择读库（如果有的话）

从方法calcUserAvgAge可以看到在JDK8的LAMBADA表达式及Stream功能下，JAVA分析处理集合数据变得极为简单，这会大大减少我们自行加工Sharding分片数据的复杂度。

接下来看DAO层

	@Component
	public class UserDaoImpl {
		
		@Autowired
		private JdbcTemplate jdbcTemplate;
		
		public void updateUser(User user){
			int update = jdbcTemplate.update("UPDATE `user` SET `name`=? WHERE `user_id`=?;",user.getName(),user.getUserId());
			Assert.isTrue(update == 1,"it should be updated!");
		}
		
		public User findUser(int userId){
			return jdbcTemplate.queryForObject("SELECT * FROM user WHERE user_id = ?", new Object[]{userId}, rowMapper);
		}
		
		@Transactional
		@MapReduce
		public List<User> findAllUsers(){
			return jdbcTemplate.query("SELECT * FROM user", rowMapper);
		}
		
		@Transactional(readOnly=true)
		@MapReduce
		public void findAllUsers(ReduceResultHolder resultHolder){
		    List<User> shardingUsers = jdbcTemplate.query("SELECT * FROM user", rowMapper);
		    resultHolder.setShardingResult(shardingUsers);
		}
	}

@MapReduce表示该方法将会在每个数据分片都执行一遍，然后进行数据聚合后返回。
对于聚合前后返回的数据类型一致的方法，调用时可以直接从返回值取得聚合结果。
对于聚合前后返回的数据类型不一致的方法，需要传入一个对象ReduceResultHolder,调用完成后，通过该对象获得聚合结果

默认情况下，框架会提供一个通用Reduce策略，如果是数字则累加返回，如果是Collection及其子类则合并后返回，如果是MAP则也是合并后返回。
如果该策略不适合，那么用户可自行设计指定Reduce策略。

@Transaction表示每一个Sharding执行的SQL都处于一个事务中，并不是表示整个聚合操作是一个整体的事务。所以，MapReduce最好不要进行更新操作（考虑框架层次限制MapReduce只允许ReadOnly事务）。

@MapReduce执行的操作会在@Transaction之前。


# 优点缺点对比
以上是框架的主要使用形式，我们可以从这种实现中发现服务层的Sharding有以下好处
* 全数据库、全SQL兼容
    * SQL层Sharding无法做到
* 能完美实现读写分离
    * 基于SQL层实现的Sharding引入读写分离后，在上层Service感知的事务里，存在混乱的隔离级别的问题，其最多实现RC级别读写分离(若不在Service层介入相关辅助代码的话)，而Service层Sharding在Service开始前就能确定该事务是读事务，整个读事务都在一个读库中完成，隔离级别与数据库一致
* 无额外维护DBProxy可用性的负担
* 相对于复杂的SQL解析，实现简单，相信花个一天就能看完所有代码，整个框架了如指掌
* 无SQL解析成本，性能更高
* 隔离级别及事务原子性等特征与使用的数据库一致，无额外学习负担，易于写出正确的程序
    * 框架限制了所有事务都在单库进行
    * 基于Sql的Sharding即使在非读写分离情况下，因其需要归并多个数据库的结果，其提供的隔离级别也是混乱的，但这个区别并没有显式的提示到程序员。


当然也存在缺点

劣势：
* 跨库查询需要自行进行结果聚合
    * 是劣势也是优势
    * 劣势：需要完成额外的聚合代码
    * 优势：但其能能更好的调优,使用JDK8的Stream及Lambada表达式，能像写SQL一样简单的完成相关集合处理
* 跨库事务需要自行保证
    * 是劣势也是优势
    * 劣势：需要额外自行实现跨库事务
    * 优势：目前所有的Sharding框架实现的跨库事务都有缺陷或者说限制，如Sharding-JDBC,Mycat等提供的跨库事务都并非严格意义的ACID，A可能被打破，I也与原生定义的不一样，程序员不熟悉时就很容易写出不可靠的代码。因此自行控制分布式事务，采用显式的事务控制或许是更好的选择。可参考使用本人写的另外一个框架EasyTransaction
* 无法实现单库分表
	* 其实，单库分表并不是必须的，这可以用数据原生的表分区来实现，性能一样，使用更便捷

# 具体使用方法
更具体使用案例请参考 测试Package：org.easydevelop.business里的案例