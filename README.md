Typescript DTO translator
=========================

This project creates interfaces and classes for Typescript based on Java DTOs. The DTOs offer a consistent handling of data in Typescript and Java applications, both for domain representation and use in API interactions, in a type-safe manner.

Simple example
--------------

Considering a Java class with three fields

```java
class Sample {
    String string;
    int integer;
    boolean bool;
}
```

an equivalent representation in Typescript would be as follows.

```typescript
interface Sample {
  string?: string,
  integer: number,
  bool: boolean
}
```

The goal of this project is to automate such translation.

Use of the API
--------------

The API is implemented with a single entry point `TypescriptDto` in the *typescript-dto-generator* module:

```java
String typescript = new TypescriptDto().make(Sample.class);
```

The class is immutable and offers different configuration steps to customize its behavior such as:
- `StructuralResolver`: Allows to adjust the structure of Java DTOs compared to their raw definition. For example, a resolver can process metadata provided by JAXB annotations.
- `condition`: Allows to remove certain properties during processing.
- `namingStrategy`: Allows to specify custom names of Typescript interfaces given a Java class.
- `typeResolver`: Allows to resolve a custom typescript type given a property type.
- `fallbackType`: Allows to set a specific type for Typescript to use when no specific type can be resolved.
- `export`: Organises the generated type as module where the resulting types are exported.
- `Implosion`: Determines if additional Typescript DTOs should be created where nested objects are flattened out to a single, flat structure. Additionally, mappers between the flattened and original DTOs are created.
- `retainFalse`: If not set, `false` values in Typescript objects will be considered default values, such that no nested objects will be created as a result of this property alone.
- `retainZero`: If not set, `0` values in Typescript objects will be considered default values, such that no nested objects will be created as a result of this property alone.
- `retainEmptyString`: If not set, empty string values in Typescript objects will be considered default values, such that no nested objects will be created as a result of this property alone.

Maven plugin
------------

Typescript types can be created within a project's build using the *typescript-dto-maven-plugin*. It is set up by configuring a module's build to execute the *typescript-dto* goal:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator</groupId>
      <artifactId>typescript-dto-translator-maven-plugin</artifactId>
      <version>LATEST</version>
      <executions>
        <execution>
          <goals>
            <goal>typescript-dto</goal>
          </goals>
          <configuration>
            <definitions>
              <definition>
                <types>
                  <type>com.acme.MyFoo</type>
                  <type>com.acme.MyBar</type>
                </types>
              </definition>
            </definitions>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Finally, it is possible to enable JAXB-specific processing by setting the `<resolver>JAVAX</resolver>` (for the *javax* namespace) or `<resolver>JAKARTA</resolver>` (for the *jakarta* namespace) configuration. It is also possible to resolve *structural-type* implementations with `<resolver>STRUCTURAL</resolver>`. Any other property can also be defined using Maven configuration.

The Maven plugin will also add a configuration to directly compile Typescript DTOs from the target location.
