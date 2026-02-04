# LaTeX Rendering Support in Android

## Overview
The Android app now supports rendering LaTeX mathematical equations and formulas in MCQ questions, options, and explanations using KaTeX.

## Implementation

### Component: LatexText
Location: `app/src/main/java/com/gatishil/studyengine/presentation/common/components/LatexText.kt`

The `LatexText` composable uses a WebView to render LaTeX content with KaTeX library loaded from CDN.

### Features
- **Inline Math**: Use `$...$` for inline equations (e.g., `$x^2 + 5x + 6 = 0$`)
- **Display Math**: Use `$$...$$` for centered display equations
- **Chemistry**: Supports mhchem extension for chemical formulas (e.g., `$\ce{H2O}$`)
- **Theme Support**: Automatically adapts to light/dark theme
- **Dynamic Height**: Content height adjusts automatically

### Usage

```kotlin
import com.gatishil.studyengine.presentation.common.components.LatexText

// In your composable
LatexText(
    text = "Solve for \$x\$ in the equation: \$x^2 + 5x + 6 = 0\$",
    modifier = Modifier.fillMaxWidth()
)
```

### LaTeX Syntax Examples

#### Basic Math
```
Inline: $x^2 + y^2 = r^2$
Display: $$\int_{0}^{\pi} \sin(x) \, dx$$
```

#### Fractions
```
$\frac{a}{b}$ or $\frac{numerator}{denominator}$
```

#### Subscripts and Superscripts
```
$x_1, x_2, ..., x_n$
$a^2 + b^2 = c^2$
```

#### Greek Letters
```
$\alpha, \beta, \gamma, \Delta, \Omega$
```

#### Matrices
```
$$\begin{pmatrix} a & b \\ c & d \end{pmatrix}$$
```

#### Chemistry (using mhchem)
```
$\ce{H2O}$
$\ce{C3H8 + 5O2 -> 3CO2 + 4H2O}$
```

## Integration Points

### 1. TakeExamScreen
- Question text: Renders LaTeX in question display
- Option text: Renders LaTeX in answer options

### 2. ExamResultScreen
- Question text: Renders LaTeX in answer review
- Option text: Renders LaTeX in option display
- Explanation: Renders LaTeX in explanations

## Backend Support

The backend already supports LaTeX text in:
- `McqQuestion.QuestionText`
- `McqQuestionOption.OptionText`
- `McqQuestion.Explanation`

Simply include LaTeX syntax in these fields when creating questions via the API.

## Requirements

- Internet permission (already configured in AndroidManifest.xml)
- JavaScript enabled in WebView (handled by LatexText component)
- KaTeX CDN access (loaded from https://cdn.jsdelivr.net)

## Performance Considerations

- WebView instances are reused when possible
- KaTeX library is cached by the browser
- Content is rendered asynchronously
- Height constraints prevent excessive memory usage

## Troubleshooting

### LaTeX not rendering
1. Check internet connectivity
2. Verify LaTeX syntax is correct
3. Check WebView JavaScript is enabled

### Content cut off
- The component uses `heightIn(min = 40.dp, max = 300.dp)`
- Adjust max height if needed for longer content

### Special characters
- Dollar signs in non-math text should be escaped: `\$`
- Backticks should be escaped: `` \` ``

## Future Enhancements

- Offline KaTeX support (bundle library in assets)
- Custom height calculation based on content
- Copy/paste support for equations
- Accessibility improvements for screen readers
