# property-change-observer

Zero-dependency Java object property change observer using a standard `javac` Annotation Processor (`@Observe`).

At compile time, classes annotated with `@Observe` automatically get a generated `Observable<ClassName>` subclass in the same package that intercepts setter mutations, compares old and new values, and publishes immutable `Change` records through `ObserverSingleton`.

## Requirements

- Java 17+ (compatible with Java 17, 21, 25+)
- No AspectJ or bytecode weaving plugins required (`javac` discovers the processor automatically via `META-INF/services/javax.annotation.processing.Processor`).

## Usage

### 1. Annotate your class with `@Observe`

```java
package com.example;

import com.variacode.utils.object.changelistener.Observe;

@Observe(stream = "animals")
public class Dog {
    private String name;
    private int daysOld;

    public Dog(String name, int daysOld) {
        this.name = name;
        this.daysOld = daysOld;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDaysOld() { return daysOld; }
    public void setDaysOld(int daysOld) { this.daysOld = daysOld; }
}
```

### 2. Subscribe to changes and instantiate `Observable<ClassName>`

```java
try (AutoCloseable sub = ObserverSingleton.INSTANCE.subscribe("animals", change -> {
    System.out.printf("Field '%s' changed from %s to %s on %s%n",
            change.fieldName(), change.oldValue(), change.fieldValue(), change.object());
})) {
    Dog trufa = new ObservableDog("Trufa", 0);
    trufa.setDaysOld(64);
}
```

## Dedication

This project is dedicated to Trufita, rest in peace.