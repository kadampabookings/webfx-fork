// Generated by WebFx

module mongoose.frontend.activities.options {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.client.aggregates;
    requires mongoose.client.bookingcalendar;
    requires mongoose.client.bookingprocess;
    requires mongoose.client.businesslogic;
    requires mongoose.client.entities;
    requires mongoose.client.icons;
    requires mongoose.client.sectionpanel;
    requires mongoose.client.util;
    requires mongoose.client.validation;
    requires mongoose.frontend.activities.person;
    requires mongoose.shared.entities;
    requires mongoose.shared.time;
    requires webfx.extras.imagestore;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.domain;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.layouts;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.orm.entity;
    requires webfx.kit.util;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.frontend.activities.options;
    exports mongoose.frontend.activities.options.routing;
    exports mongoose.frontend.operations.options;

    // Provided services
    provides webfx.framework.client.ui.uirouter.UiRoute with mongoose.frontend.activities.options.OptionsUiRoute;

}