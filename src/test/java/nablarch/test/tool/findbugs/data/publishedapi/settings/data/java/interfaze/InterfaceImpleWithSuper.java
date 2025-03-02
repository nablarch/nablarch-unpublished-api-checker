package nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze;

import nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.superclass.Super;

public class InterfaceImpleWithSuper extends Super implements InterfaceFor1Interface {

    @Override
    public void interfaceMethod() {
    }

    @Override
    public void interfaceDefaultMethod() {
    }

    @Override
    public void superMethod() {
    }

    public void impleOnlyMethod() {
    }
}
