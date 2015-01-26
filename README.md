# property-change-observer
Java object property change observer, produces streams of object changes using the @Observe annotation on the classes you want to follow

You will need to add the following to your POM, considering the java version you use:

```xml
	<build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>1.7</version>
                <configuration>
                    <complianceLevel>1.8</complianceLevel>
                    <source>1.8</source>
                    <target>1.8</target>v
                    <aspectLibraries>
                        <aspectLibrary>
                        	<groupId>com.variacode.utils.object.changelistener</groupId>
    						<artifactId>PropertyChangeObserver</artifactId>
                        </aspectLibrary>
                    </aspectLibraries>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```xml

## .

This project is dedicated to Trufita, rest in peace.