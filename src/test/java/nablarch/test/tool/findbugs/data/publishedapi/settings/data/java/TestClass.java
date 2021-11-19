package nablarch.test.tool.findbugs.data.publishedapi.settings.data.java;

public class TestClass {

    public void testMethod() {
    }

    public void testMethod2() {
    }
    
    public void testMethod3() {
    }
    
    private void privateMethod() {
    }

    public static class OK {
        public OK() {
            this("");
        }

        public OK(String... s) {
        }

        public boolean isHoge() {
            return true;
        }
    }
    public static class NG {
    }
}
