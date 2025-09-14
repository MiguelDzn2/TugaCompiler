package VM;

import java.util.ArrayList;

public class RuntimeStack {
    private ArrayList<Object> stack;

    public RuntimeStack(){
        this.stack = new ArrayList<>();
    }

    public Object pop(){
        Object result = this.stack.getLast();
        this.stack.removeLast();
        return result;
    }

    public void push(Object o){
        this.stack.add(o);
    }

    public Object get(int i){
        return this.stack.get(i);
    }

    public void set(int i, Object o){
        this.stack.set(i, o);
    }

    public int size(){
        return stack.size();
    }

    @Override
    public String toString(){
        return stack.toString();
    }
}
