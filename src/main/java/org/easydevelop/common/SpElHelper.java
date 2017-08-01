package org.easydevelop.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

/** 
* @author xudeyou 
*/
public class SpElHelper {
	
	private ExpressionParser parser = new SpelExpressionParser();
	private ConcurrentHashMap<String,Expression> mapExpression = new ConcurrentHashMap<>();
	private ThreadLocal<StandardEvaluationContext> evaluationContext = new ThreadLocal<>();
	@Autowired
	private ApplicationContext beanFactory;
	private BeanFactoryResolver beanResolver;
	
	@PostConstruct
	private void init(){
		beanResolver = new BeanFactoryResolver(beanFactory);
	}
	
	private Expression getExpression(String expressionStr) {
		Expression expression = mapExpression.get(expressionStr);
		if(expression == null){
			expression = parser.parseExpression(expressionStr);
			mapExpression.put(expressionStr, expression);
		} 
		return expression;
	}
	
	@SuppressWarnings("unchecked")
	public <R> R getValue(String spEl,Object root){
		Expression exp = getExpression(spEl);
		StandardEvaluationContext standardEvaluationContext = evaluationContext.get();
		if(standardEvaluationContext == null){
			standardEvaluationContext = new StandardEvaluationContext();
			standardEvaluationContext.setBeanResolver(beanResolver);
			evaluationContext.set(standardEvaluationContext);
		}
		
		standardEvaluationContext.setRootObject(root);
		return (R) exp.getValue(standardEvaluationContext);
	}
	
	public <R> R getValue(String spEl){
		return getValue(spEl, null);
	}
	
	public void setValue(String spEl,Object root,Object value){
		Expression exp = getExpression(spEl);
		StandardEvaluationContext standardEvaluationContext = evaluationContext.get();
		if(standardEvaluationContext == null){
			standardEvaluationContext = new StandardEvaluationContext();
			standardEvaluationContext.setBeanResolver(beanResolver);
			evaluationContext.set(standardEvaluationContext);
		}
		
		standardEvaluationContext.setRootObject(root);
		exp.setValue(standardEvaluationContext, value);
	}

}
