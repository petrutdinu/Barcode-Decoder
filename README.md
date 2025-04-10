# 📦 Barcode Decoder

---

## 🔍 Overview

This project implements a functional Scala solution for decoding EAN-13 barcodes from black-and-white image data. It performs bit-level analysis and pattern recognition to identify the 13-digit barcode number based on the standard encoding structure.

The main steps of the decoding process include:

- Conversion of characters and integers to binary bits.
- Grouping and encoding of bits using run-length encoding (RLE).
- Normalization of bar widths as fractional ratios.
- Pattern matching against known digit encodings using distance metrics.
- Barcode reconstruction and validation, including control digit checking.

---

## 🧱 Features & Modules

### 🧮 Bit Manipulation (Section 1.1–1.2)

- `toBit`: Converts characters or integers (`0`, `1`) to custom `Bit` types (`Zero`, `One`).
- `complement`: Inverts a bit.

### 🧾 Digit Encodings (Section 1.3)

- Predefined patterns for **L** (Odd parity), **G** (Even parity), and **R** (Right-side) encodings.
- `leftOddList`, `leftEvenList`, and `rightList` represent these patterns as lists of `Bit`.

### 🧵 Run-Length Encoding (Section 1.4–1.5)

- `group`: Groups consecutive equal elements.
- `runLength`: Converts a list to a list of (count, element) pairs.

### ⚖️ Ratio Calculations (Section 2–3)

- `RatioInt`: Custom fraction class for ratio operations and comparisons.
- `scaleToOne`: Normalizes run-length values to sum up to 1.
- `scaledRunLength`: Applies normalization to RLE-encoded bit sequences.

### 🎯 Digit Matching & Distance Metrics (Section 4.1–4.4)

- `distance`: Computes the difference between two scaled encodings.
- `bestMatch`: Finds the closest digit match from a list of known SRL codes.
- `bestLeft` / `bestRight`: Determines whether an SRL pattern is best matched as a left-side or right-side digit.

### 🛠️ Barcode Reconstruction (Section 4.5–4.9)

- `findLast12Digits`: Extracts the last 12 digits from a barcode pattern.
- `firstDigit`: Infers the first digit based on parity patterns.
- `checkDigit`: Computes the EAN-13 check digit.
- `verifyCode`: Validates the 13-digit barcode.
- `solve`: Complete end-to-end barcode decoding logic from run-length input.

### 🧰 Utility (Pixel Input Processing)

- `checkRow`: Processes a list of pixels (black/white values) and extracts possible barcode regions based on EAN-13 structure.

---

## 🧪 Testing

The project includes a comprehensive test suite located in `src/test/Test.scala`. These tests cover all major functions and are grouped by tasks as defined in the assignment:

- ✅ Bit operations and encoding tables
- ✅ Grouping and run-length encoding
- ✅ Ratio computations and normalization
- ✅ Barcode digit matching and reconstruction
- ✅ Full validation of EAN-13 codes

You can run all tests from IntelliJ or the command line:

```bash
sbt test
```

---

## 🚀 Usage

You can decode barcodes from your own images using the helper app provided in `MyBarcodes.scala`.

### Steps:

1. Create a folder named `MyBarcodesInput` in the project root.
2. Place `.ppm` images containing barcode regions inside that folder.
3. Run the program:

```bash
sbt "runMain MyBarcodes"
```

This will output results and processed `.pbm` images in the `MyBarcodesOutput` folder.

### Using functions manually:

If you want to manually test with image pixel data:

```scala
// From a PBM row (List[Pixel]), extract RLE-encoded barcode segments
val segments: List[List[(Int, Bit)]] = Decoder.checkRow(row: List[Pixel])

// Try decoding the first identified segment
val maybeBarcode: Option[String] = Decoder.solve(segments.head)
```

---

## 🔄 Example Flow

1. **Input**: A list of `Pixel` values (e.g., from a scanline in an image).
2. **Run-Length Encoding**: Apply `runLength` or `checkRow`.
3. **Decoding**: Use `solve` to extract and validate the barcode.

---