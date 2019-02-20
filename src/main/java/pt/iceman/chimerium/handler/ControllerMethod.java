package pt.iceman.chimerium.handler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerMethod {
    private Class<?>[] argumentsTypes;
    private Class<?> bodyType;
    private List<Object> args;
    private Method method;

    public ControllerMethod(Class<?>[] argumentsTypes, Method method) {
        this.argumentsTypes = argumentsTypes;
        this.method = method;
    }

    public ControllerMethod(Class<?>[] argumentsTypes, Class<?> bodyType, Method method) {
        this.argumentsTypes = argumentsTypes;
        this.bodyType = bodyType;
        this.method = method;
    }

    public Class<?>[] getArgumentsTypes() {
        return argumentsTypes;
    }

    public Class<?> getBodyType() {
        return bodyType;
    }

    public Method getMethod() {
        return method;
    }

    public List<Object> getArgs(List<String> routeValues) throws NumberFormatException, IllegalArgumentException {
        return IntStream.range(0, routeValues.size())
                        .mapToObj(i -> castArgument(routeValues.get(i), this.argumentsTypes[i]))
                        .collect(Collectors.toList());
    }

    private Object castArgument(String arg, Class type) throws NumberFormatException, IllegalArgumentException {
        Object o = null;

        switch (type.getName()) {
            case "java.lang.String":
                o = arg;
                break;
            case "java.util.UUID":
                o = UUID.fromString(arg);
                break;
            case "java.lang.Integer":
                o = Integer.parseInt(arg);
                break;
            case "java.lang.Long":
                o = Long.parseLong(arg);
                break;
            case "java.lang.Double":
                o = Double.parseDouble(arg);
                break;
            case "java.lang.Float":
                o = Float.parseFloat(arg);
                break;
        }

        return o;
    }
}
