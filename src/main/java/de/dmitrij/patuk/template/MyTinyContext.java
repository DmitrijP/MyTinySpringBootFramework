package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.nodes.MyTinyPropertyPath;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

//This handles the scopes we enter and exit during matching of our model to our PropertyPaths
//model ("model" -> model)
//|->employee ("employee"   -> model.employee)
//  |->profile ("profile"   -> model.employee.profile)
//      |->name ("name"     -> model.employee.profile.name)
public class MyTinyContext {
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();

    //the root scope is the main model
    public MyTinyContext(Object model) {
        scopes.push(new HashMap<>());
        scopes.peek().put("model", model);
    }

    //we set a new variable in the current scope
    public void set(String name, Object value) {
        scopes.peek().put(name, value);
    }

    //a new scope is always an empty hashmap that we fill with objects
    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    //we pop the whole hashmap with all the objects since we left that scope
    public void popScope() {
        scopes.pop();
    }

    //here we iterate over the identifiers inside a property path,
    //and call the right getMethod
    public Object resolve(MyTinyPropertyPath path) {
        // path: identifier(.identifier)*
        Object current = resolveIdentifier(path.getParts().get(0).getName());
        for (int i = 1; i < path.getParts().size(); i++) {
            current = accessProperty(current, path.getParts().get(i).getName());
        }
        return current;
    }

    //what do we do here?
    //we have multiple scopes
    //model ("model" -> model)
    //|->employee ("employee"   -> model.employee)
    //  |->profile ("profile"   -> model.employee.profile)
    //      |->name ("name"     -> model.employee.profile.name)
    //each of these steps is its own Map<String, Object> scope
    //so we iterate over the scopes until we find the right one
    private Object resolveIdentifier(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    // we only handle getMethods here
    // so we prepend get and capitalize
    // than we call that method to get that object out of it
    private Object accessProperty(Object obj, String property) {
        // reflection or Map handling
        if (obj instanceof Map<?, ?> map) {
            return map.get(property);
        }
        try {
            var method = obj.getClass().getMethod("get" + capitalize(property));
            return method.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
