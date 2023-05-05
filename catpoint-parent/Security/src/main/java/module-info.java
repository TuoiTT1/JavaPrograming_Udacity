module Security {
    requires java.desktop;
    requires java.prefs;
    requires com.miglayout.swing;
    requires com.google.common;
    requires com.google.gson;
    requires Image;
    opens com.udacity.security.data to com.google.gson;
}