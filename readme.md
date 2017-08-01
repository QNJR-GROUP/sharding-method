# 设计初衷

总的来说，DBProxy，Sql/DAO层的Sharding方案都希望提供一个屏蔽底层Sharding逻辑的解决方案，然而，这仅仅只是一个美丽的目标。因种种原因，这些层次的Sharding方案都无法提供跟原生数据库一样的功能（ACID特性、SQL支持等）。

更为合理的Sharding出发点应该在Service层，由Service层的信息决定Sharding的走向.

Service层Sharding的优劣势
优势：
* 全数据库、全SQL兼容
    * SQL层Sharding无法做到
* 能完美实现读写分离
    * SQL/DAO层实现的Sharding引入读写分离后，在上层Service感知的事务里，存在混乱的隔离级别的问题，其最多实现RC级别读写分离，而Service层Sharding在Service开始前就能确定Sharding对应的库，隔离级别与数据库一致
* 事务隔离级别及事务原子性等特征与使用的数据库一致，无额外学习负担，易于写出正确的程序
    * 基于Sql的Sharding即使在非读写分离情况下，因其需要归并多个数据库的结果，其提供的隔离级别也是混乱的，这个区别并没有显式的提示到程序员
* 无额外维护DBProxy可用性的负担
* 相对于复杂的SQL解析，实现简单，BUG存在可能性更低，学习成本及扩展定制成本也低
* 无SQL解析成本，性能更高

劣势：
* 跨库查询需要自行进行结果聚合
    * 是劣势也是优势
    * 劣势：需要完成额外的聚合代码
    * 优势：能更好的调优
* Sharding层对Service不透明
    * 是劣势也是优势
    * 劣势：Service层代码感知到底层不止一个数据库，迁移到TiDB等分布式数据库时，对于聚合类查询，如果不做调整，可能会有些冗余代码
    * 优势：针对目前更容易写出高效正确可靠的代码，更何况切换到分布式数据库是得多久之后？
* 跨库事务需要自行保证
    * 是劣势也是优势
    * 劣势：需要额外自行实现跨库事务
    * 优势：目前所有的Sharding框架实现的跨库事务都有缺陷或者说限制，如Sharding-JDBC,Mycat等提供的跨库事务都并非严格意义的ACID，A可能被打破，I也与原生定义的不一样，程序员不熟悉时就很容易写出不可靠的代码。因此自行控制分布式事务，采用显式的事务控制或许是更好的选择。可参考使用本人写的另外一个框架EasyTransaction
* 无法实现单库分表
	* 其实，单库分表并不是必须的，这可以用数据原生的表分区来实现，性能一样，使用更便捷


因无法屏蔽这些差异，程序员写代码时，如果要写出正确可靠的代码，必须清楚的了解其具体Sharding实现与原生数据库的不同，才能写出正确的代码。

并且在写这些代码的过程中，与原生单机数据库使用时的差异关注点都隐式的隐藏于SQL或者DAO中，这不便于REVIEW与问题的发现

既然如此，我们倒不如直接Sharding后与原生单机数据库不一致的关注点摆上台面，让Service层感知到差异，并以正确适当的方式处理掉这些差异。

以上就是本框架的设计初衷。




# 使用简介

目前这个框架的版本，只是一个初具雏形的DEMO，有很多细节没有优化，但已经基本可用。以下展示框架的基本用法

以下为Service层代码

	@Service
	
	//Sharding这个注解表征这个Sevice存在方法需要进行Sharding,以选择特定的数据库
	//defaultDsSet表示Sharding时默认使用的一组同构的Sharding数据源，其可被改写
	//defaultKeyEls表示具体方法里哪个参数的哪些位置表示Sharding的Key,框架根据这些Key的值进行数据源选择,其为SpEL表达式。
	//			"[user].userId" 表示 参数名为user的对象的字段userId将会传入ShardingStrategy中进行计算，以获得Sharding分片号
	//defaultStrategy表示分片选择的策略传入defaultKeyEls选择出来的Value，返回分片位置
	@Sharding(defaultDsSet="orderSet",defaultKeyEls="[user].userId",defaultStrategy=TestApplicationConfig.BY_USER_ID_MOD)
	
	//KeyGenerate这个注解表征这个Service存在方法需要为其参数填充ID
	//defaultKeyEls表示需要填充生成ID的参数位置
	//defaultStrategy表征生成ID的策略
	@KeyGenerate(defaultKeyEls="[user].userId",defaultStrategy=TestApplicationConfig.INT_INCREASE)
	public class UserServceImpl {
		
		
		@Autowired
		private UserDaoImpl userDao;
		
		@Transactional
		//表示本方法需要进行Sharding，要在Transaction开始前，框架会自动根据user.getUserId()选择对应数据源
		@ShardingMethod
		public void updateUser(User user){
			userDao.updateUser(user);
		}
		
		@Transactional
		//本标签表示本方法存在参数需要注入生成的ID，在这里框架将会把自动生成的ID，设置到user.userId里
		@KeyInject
		//框架根据生成的userId自动选择对应的数据源
		@ShardingMethod
		public void saveUser(User user){
			userDao.saveUser(user);
		}
		
		@Transactional
		@ShardingMethod(keyNameEls="[userId]")//修改默认的ShardingKey取值位置
		public void deleteUser(int userId){
			userDao.deleteUser(userId);
		}
		
		@Transactional
		@ShardingMethod(keyNameEls="[userId]")
		public User findUser(int userId){
			return userDao.findUser(userId);
		}
		
		//对于跨分片的操作，不在Service层加Transaction注解，
		//因为即使加了Transaction，也无法保证ACID，Transation标签在DAO层添加，后续会说明
		public List<User> findAllUsers(){
			return userDao.findAllUsers();
		}
		
		public int deleteAllUsers(){
			return userDao.deleteAllUsers();
		}
	
	}

以下为DAO层代码

	@Component
	
	//Aggregation标签表明这个类下，存在方法需要进行访问所有数据库，逐一获得返回并聚合处理结果
	//defaultDsSet表示默认需要聚合的数据源集合
	@Aggregation(defaultDsSet="orderSet")
	public class UserDaoImpl {
		
		@Autowired
		private JdbcTemplate jdbcTemplate;
		
		public void saveUser(User user){
			int update = jdbcTemplate.update("INSERT INTO `user` (`user_id`, `name`) VALUES (?, ?)",user.getUserId(),user.getName());
			Assert.isTrue(update == 1,"it should be inserted!");
		}
		
		public void updateUser(User user){
			int update = jdbcTemplate.update("UPDATE `user` SET `name`=? WHERE `user_id`=?;",user.getName(),user.getUserId());
			Assert.isTrue(update == 1,"it should be updated!");
		}
	
		public void deleteUser(int userId) {
			int update = jdbcTemplate.update("DELETE FROM `user` WHERE `user_id`=?;",userId);
			Assert.isTrue(update == 1,"it should be deleted!");
		}
		
		private BeanPropertyRowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
		public User findUser(int userId){
			return jdbcTemplate.queryForObject("SELECT * FROM user WHERE user_id = ?", new Object[]{userId}, rowMapper);
		}
		
		//这个标签表明下面这个方法需要对每个分片数据库都调用一遍，并且将调用后的结果统一传给特定的聚合处理类进行处理，最后统一返回处理后的结果
		//外面的Service调用这个findAllUsers()将能获得所有数据库里的所有用户记录
		@AggregationMethod(strategy=TestApplicationConfig.AGGREGATION_USER_ORDER_BY_USER_ID)
		//这里加上Transactional标志，使得每个分片的读写操作都能在事务里，这样每个分片的事务隔离级别都是清晰的。
		@Transactional
		public List<User> findAllUsers(){
			return jdbcTemplate.query("SELECT * FROM user", rowMapper);
		}
		
		@Transactional
		@AggregationMethod(strategy=TestApplicationConfig.UPDATE_COUNT_ADD)
		public int deleteAllUsers(){
			int update = jdbcTemplate.update("delete from user");
			System.out.println(update);
			return update;
		}
	
	}


更多使用的细节请大家参考UT里的案例，更多实现的细节请自行研究代码
    
