package naga.fx.properties;

import emul.javafx.beans.property.Property;
import emul.javafx.beans.property.SimpleObjectProperty;
import emul.javafx.beans.value.ChangeListener;
import emul.javafx.beans.value.ObservableValue;
import naga.fx.spi.Toolkit;
import naga.util.collection.Collections;
import naga.util.function.Consumer;
import naga.util.function.Func2;
import naga.util.function.Function;
import naga.util.function.Predicate;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public final class Properties {

    public static Unregisterable runOnPropertiesChange(Consumer<ObservableValue> consumer, ObservableValue... properties) {
        return new UnregisterableListener(consumer, properties);
    }

    public static Unregisterable runNowAndOnPropertiesChange(Consumer<ObservableValue> consumer, ObservableValue... properties) {
        consumer.accept(properties.length == 1 ? properties[0] : null);
        return runOnPropertiesChange(consumer, properties);
    }

    // Same API but with Collection instead of varargs

    public static Unregisterable runNowAndOnPropertiesChange(Consumer<ObservableValue> consumer, Collection<ObservableValue> properties) {
        return runNowAndOnPropertiesChange(consumer, Collections.toArray(properties, ObservableValue[]::new));
    }

    public static Unregisterable runOnPropertiesChange(Consumer<ObservableValue> consumer, Collection<ObservableValue> properties) {
        return runOnPropertiesChange(consumer, Collections.toArray(properties, ObservableValue[]::new));
    }

    // Same API but with Runnable instead of Consumer

    public static Unregisterable runOnPropertiesChange(Runnable runnable, ObservableValue... properties) {
        return runOnPropertiesChange(p -> runnable.run(), properties);
    }

    public static Unregisterable runNowAndOnPropertiesChange(Runnable runnable, ObservableValue... properties) {
        return runNowAndOnPropertiesChange(p -> runnable.run(), properties);
    }

    public static Unregisterable runNowAndOnPropertiesChange(Runnable runnable, Collection<ObservableValue> properties) {
        return runNowAndOnPropertiesChange(p -> runnable.run(), properties);
    }

    public static Unregisterable runOnPropertiesChange(Runnable runnable, Collection<ObservableValue> properties) {
        return runOnPropertiesChange(p -> runnable.run(), properties);
    }


    public static <T, R> ObservableValue<R> compute(ObservableValue<? extends T> p, Function<? super T, ? extends R> function) {
        Property<R> combinedProperty = new SimpleObjectProperty<>();
        runNowAndOnPropertiesChange(arg -> combinedProperty.setValue(function.apply(p.getValue())), p);
        return combinedProperty;
    }

    public static <T1, T2, R> ObservableValue<R> combine(ObservableValue<? extends T1> p1, ObservableValue<? extends T2> p2, Func2<? super T1, ? super T2, ? extends R> combineFunction) {
        Property<R> combinedProperty = new SimpleObjectProperty<>();
        runNowAndOnPropertiesChange(arg -> combinedProperty.setValue(combineFunction.call(p1.getValue(), p2.getValue())), p1, p2);
        return combinedProperty;
    }

    public static <T> ObservableValue<T> filter(ObservableValue<T> property, Predicate<T> predicate) {
        Property<T> filteredProperty = new SimpleObjectProperty<>();
        runNowAndOnPropertiesChange(arg -> {
            if (predicate.test(property.getValue())) filteredProperty.setValue(property.getValue());
        }, property);
        return filteredProperty;
    }

    public static <T> void consume(ObservableValue<T> property, Consumer<T> consumer) {
        runNowAndOnPropertiesChange(p -> consumer.accept(property.getValue()), property);
    }

    public static <T> void consumeInUiThread(ObservableValue<T> property, Consumer<T> consumer) {
        consume(property, arg -> Toolkit.get().scheduler().scheduleDeferred(() -> consumer.accept(arg)));
    }

    public static <T> void setIfNotBound(Property<T> property, T value) {
        if (!property.isBound())
            property.setValue(value);
    }

    public static <T> void onPropertySet(ObservableValue<T> property, Consumer<T> valueConsumer) {
        onPropertySet(property, valueConsumer, false);
    }

    public static <T> void onPropertySet(ObservableValue<T> property, Consumer<T> valueConsumer, boolean callIfNullProperty) {
        if (property == null) {
            if (callIfNullProperty)
                valueConsumer.accept(null);
        } else {
            T value = property.getValue();
            if (value != null)
                valueConsumer.accept(value);
            else
                property.addListener(new ChangeListener<T>() {
                    @Override
                    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                        if (newValue != null) {
                            observable.removeListener(this);
                            valueConsumer.accept(newValue);
                        }
                    }
                });
        }
    }
}
