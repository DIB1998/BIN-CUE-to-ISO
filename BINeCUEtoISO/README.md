BINeCUEtoISO — BIN to ISO conversion helper

This project converts PS2/PS1 BIN+CUE images into ISO files by extracting the data portion of sectors.

Key behaviors:
- Parses `CUE` files to determine `TRACK 01` mode and (when available) track end via `INDEX`.
- Supports `MODE1/2048`, `MODE1/2352` (data offset 16), and `MODE2/2352` (data offset 24).
- If the CUE lacks a supported mode, the converter attempts to detect mode by scanning the BIN for ISO signature (`CD001`).
- Warns when the CUE references a different BIN filename, and when the BIN ends with a partial sector (the final partial sector is ignored).

Notes and limitations:
- Multi-track and mixed-mode discs are not fully supported; this tool focuses on extracting a single data track (TRACK 01).
- The detection heuristic is conservative; if detection fails, conversion will abort with a helpful message.

If you want additional behaviors from the Windows `PS2 BIN 2 ISO` executable bundled in the `PS2 BIN 2 ISO v0.1` folder, tell me which behavior to replicate (e.g., special handling of mixed-mode cues or command-line compatibility) and I will prioritize it.
