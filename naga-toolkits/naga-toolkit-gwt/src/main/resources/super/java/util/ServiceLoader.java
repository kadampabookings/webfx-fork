package java.util;

/**
 * ServiceLoader GWT implementation that just works to provide the GwtPlatform on automatic registration Platform.get()
 *
 * @author Bruno Salmon
 */

import naga.commons.util.numbers.providers.StandardPlatformNumbers;
import naga.commons.util.numbers.spi.NumbersProvider;
import naga.toolkit.spi.Toolkit;
import naga.platform.spi.Platform;
import naga.platform.providers.gwt.GwtPlatform;
import naga.toolkit.providers.gwt.GwtToolkit;

public class ServiceLoader<S> {

    public static <S> ServiceLoader<S> load(Class<S> service) {
        if (service.equals(Platform.class))
            return new ServiceLoader<>(new GwtPlatform());
        if (service.equals(Toolkit.class))
            return new ServiceLoader<>(new GwtToolkit());
        if (service.equals(NumbersProvider.class))
            return new ServiceLoader<>(new StandardPlatformNumbers());
        return null;
    }

    private final Object service;

    public ServiceLoader(Object service) {
        this.service = service;
    }

    public Iterator<S> iterator() {
        ArrayList list = new ArrayList();
        list.add(service);
        return (Iterator<S>) list.iterator();
    }
}