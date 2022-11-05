package org.example;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jndi.JndiLocatorDelegate;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Properties;

@Configuration
@ConditionalOnProperty( "custom.jms.jndi-name" )
@ConditionalOnMissingBean( ConnectionFactory.class )
@EnableConfigurationProperties( { CustomJmsProperties.class } )
@AutoConfigureAfter( { JndiConnectionFactoryAutoConfiguration.class } )
public class EJBConfig {

    @Bean
    public ConnectionFactory connectionFactory( CustomJmsProperties customJmsProperties ) throws NamingException {
        ConnectionFactory connectionFactory = lookupForConnectionFactory( customJmsProperties );
        return getEnhancedUserCredentialsConnectionFactory( customJmsProperties, connectionFactory );
    }

    private ConnectionFactory lookupForConnectionFactory( final CustomJmsProperties customJmsProperties ) throws NamingException {
        JndiLocatorDelegate jndiLocatorDelegate = new JndiLocatorDelegate();
        Properties jndiProperties = getJndiProperties( customJmsProperties );
        jndiLocatorDelegate.setJndiEnvironment( jndiProperties );
        return jndiLocatorDelegate.lookup( customJmsProperties.getJndiName(), ConnectionFactory.class );
    }

    private Properties getJndiProperties( final CustomJmsProperties customJmsProperties ) {
        Properties jndiProperties = new Properties();
        jndiProperties.setProperty( Context.PROVIDER_URL, customJmsProperties.getProviderUrl() );
        jndiProperties.setProperty( Context.INITIAL_CONTEXT_FACTORY, customJmsProperties.getContextFactoryClass() );
        if ( StringUtils.isNotEmpty( customJmsProperties.getUsername() ) ) {
            jndiProperties.setProperty( Context.SECURITY_PRINCIPAL, customJmsProperties.getUsername() );
        }
        if ( StringUtils.isNotEmpty( customJmsProperties.getPassword() ) ) {
            jndiProperties.setProperty( Context.SECURITY_CREDENTIALS, customJmsProperties.getPassword() );
        }
        return jndiProperties;
    }

    private UserCredentialsConnectionFactoryAdapter getEnhancedUserCredentialsConnectionFactory( final CustomJmsProperties customJmsProperties,
                                                                                                 final ConnectionFactory connectionFactory ) {
        UserCredentialsConnectionFactoryAdapter enhancedConnectionFactory = new UserCredentialsConnectionFactoryAdapter();
        enhancedConnectionFactory.setTargetConnectionFactory( connectionFactory );
        enhancedConnectionFactory.setUsername( customJmsProperties.getUsername() );
        enhancedConnectionFactory.setPassword( customJmsProperties.getPassword() );
        enhancedConnectionFactory.afterPropertiesSet();
        return enhancedConnectionFactory;
    }
}
