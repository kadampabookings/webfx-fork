package naga.framework.services.i18n.spi.impl;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import naga.framework.services.i18n.Dictionary;
import naga.framework.services.i18n.spi.I18nProvider;
import naga.fx.spi.Toolkit;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Bruno Salmon
 */
public final class I18nProviderImpl implements I18nProvider {

    private final Map<Object, Reference<StringProperty>> translations = new HashMap<>();
    private boolean dictionaryLoadRequired;
    private DictionaryLoader dictionaryLoader;
    private Set<Object> unloadedKeys;

    public I18nProviderImpl(DictionaryLoader dictionaryLoader) {
        this(dictionaryLoader, "en");
    }

    public I18nProviderImpl(DictionaryLoader dictionaryLoader, Object initialLanguage) {
        this.dictionaryLoader = dictionaryLoader;
        languageProperty.addListener((observable, oldValue, newValue) -> onLanguageChanged());
        setLanguage(initialLanguage);
    }

    private Property<Object> languageProperty = new SimpleObjectProperty<>();
    @Override
    public Property<Object> languageProperty() {
        return languageProperty;
    }

    private Property<Dictionary> dictionaryProperty = new SimpleObjectProperty<>();
    @Override
    public Property<Dictionary> dictionaryProperty() {
        return dictionaryProperty;
    }

    @Override
    public ObservableStringValue translationProperty(Object key) {
        StringProperty translationProperty = getTranslationProperty(key);
        if (translationProperty == null)
            synchronized (translations) {
                translations.put(key, new WeakReference<>(translationProperty = createTranslationProperty(key)));
            }
        return translationProperty;
    }

    private StringProperty getTranslationProperty(Object key) {
        Reference<StringProperty> ref = translations.get(key);
        return ref == null ? null : ref.get();
    }

    private StringProperty createTranslationProperty(Object key) {
        return updateTranslation(new SimpleStringProperty(), key);
    }

    private void onLanguageChanged() {
        dictionaryLoadRequired = true;
        updateTranslations();
    }

    private synchronized void updateTranslations() {
        synchronized (translations) {
            for (Iterator<Map.Entry<Object, Reference<StringProperty>>> it = translations.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Object, Reference<StringProperty>> entry = it.next();
                Reference<StringProperty> value = entry.getValue();
                StringProperty translationProperty = value == null ? null : value.get();
                if (translationProperty == null)
                    it.remove();
                else
                    updateTranslation(translationProperty, entry.getKey());
            }
        }
    }

    private StringProperty updateTranslation(StringProperty translationProperty, Object key) {
        if (dictionaryLoadRequired && dictionaryLoader != null) {
            if (unloadedKeys != null)
                unloadedKeys.add(key);
            else {
                unloadedKeys = new HashSet<>();
                unloadedKeys.add(key);
                Toolkit.get().scheduler().scheduleDeferred(() -> {
                    dictionaryLoader.loadDictionary(getLanguage(), unloadedKeys).setHandler(asyncResult -> {
                        dictionaryProperty.setValue(asyncResult.result());
                        dictionaryLoadRequired = false;
                        updateTranslations();
                    });
                    unloadedKeys = null;
                });
            }
        } else
            translationProperty.setValue(instantTranslate(key));
        return translationProperty;
    }
}
