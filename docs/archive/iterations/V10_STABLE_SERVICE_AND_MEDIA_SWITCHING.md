# IceCam v10 - stable service and media switching

Changes:

- App label changed to `IceCam`.
- Fixed service name: `privsam_service`.
- Random ServerName generation removed.
- Binder client prefers `privsam_service` first.
- Root bootstrap passes `/data/vcplax privsam_service`.
- Launch is robust: if `/data/vcplax` is unavailable, `/data/camera/vcplax` is used.
- Media switching now uses a safer sequence:
  1. TX25 reset/stop
  2. TX22 range reset `0..-1`
  3. TX14 source/mode string
  4. TX11 play source
  5. TX24 apply transform

Reason:

v9 proved that root/bootstrap/Binder were functional, but media replacement could break after changing the selected file. The most likely cause was reusing the decoder/renderer state without a reset. v10 keeps native libraries untouched and fixes the Java control order only.
