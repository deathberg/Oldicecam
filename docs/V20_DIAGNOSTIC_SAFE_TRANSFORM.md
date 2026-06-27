# v20 Diagnostic Safe Transform

Problem observed from runtime log: native daemon and Binder start correctly, but each control tap still triggers high-resolution JPEG bake followed by TX14/TX11 replay. That creates visible stream gaps and makes replacement fragile under rapid interaction.

Changes:
- Transform buttons now update TransformState only.
- Apply now performs one explicit bake/replay.
- MainActivity and FloatService both use this safe behavior.
- Runtime log now includes monotonic sequence, uptime, thread name and Java heap usage.
- Export log now includes a full diagnostic snapshot: prefs, files, baked cache, Binder diagnostics, root service check, vcplax logs and filtered logcat.

This is a stabilization/diagnostic build before the realtime GPU renderer.
