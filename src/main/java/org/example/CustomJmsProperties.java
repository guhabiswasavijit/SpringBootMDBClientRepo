package org.example;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties( prefix = "custom.jms" )
public class CustomJmsProperties {
    private String jndiName;
    private String contextFactoryClass;
    private String providerUrl;
    private String username;
    private String password;
}