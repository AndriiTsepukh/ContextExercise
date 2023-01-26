package testclasses;

import org.bibernate.Autowire;
import org.bibernate.Bean;

@Bean("autowiredBean")
public class CustomAutowiredClass {
    @Autowire
    public CustomClassForAutowire customClassForAutowire;
}