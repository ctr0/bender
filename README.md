### **Bender**
**Bender** is a framework to apply the visitor design pattern to abstract source entities.  
The default project contains the source for Java beans.

To apply the visitor design pattern you only need to write a visitor class for the desired abstract source.

#### **Bender Beans**
For the following Java bean:

    public class Person {

        private String name;
        private int age;
        private List<Person> parents;

        public String getName() {
            return name;
        }
        public int getAge() {
            return age;
        }
    }

You can write a visitor like:

    public class PersonVisitor implements BenderVisitor {

        @Bender("this")
        public void visitPerson(@Bender("name") String name, @Bender("this.age") int age) {
            // Do something...
        }

        @Bender("this.parents")
        public PersonVisitor visitParents(@Bender("length") int parentsLength) {
            if (parentsLength > 0) {
                  return new PersonVisitor();
            }
            return null;
        }

        public void visitEnd() {
            // Do something...
        }
    }

Note that method parameters are annotated relative to the method target property.     
Visitors can be stacked to accept the method target property simply returning a new visitor in that method. If this property is a primitive array or an instance of `Iterable` interface, each object will accept the new visitor instance.


#### **Bender XML**
