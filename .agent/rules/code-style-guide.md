---
trigger: always_on
---

Omiri Compose UI Code Style Guide
0) Golden rules

Component-first: use existing composables before creating anything new.

Design-locked: do not change spacing, colors, shapes, typography, elevation unless explicitly requested.

No hex in screens: don’t write Color(0x...) in feature code. Add tokens to AppColors (or use MaterialTheme.colorScheme).

You run, I fix: I won’t run code. You paste errors/logcat/stack trace, I reply with minimal diffs.

1) Theme usage (mandatory)
Always wrap app UI with:
OmiriTheme {
  // NavHost / Screen
}

In composables, prefer Material tokens:

Colors: MaterialTheme.colorScheme.*

Typography: MaterialTheme.typography.*

Shapes: MaterialTheme.shapes.* (your AppShapes)

Never “invent” new sizes/styles in a screen.

Allowed direct usage of AppColors:

Extended palette / semantic tokens you already defined (e.g. AppColors.Green600, AppColors.Red600, hero colors).

But default UI should still follow MaterialTheme.colorScheme first.

2) Color rules (based on your setup)
Primary brand actions

Use MaterialTheme.colorScheme.primary (maps to AppColors.BrandOrange)

Text on primary: MaterialTheme.colorScheme.onPrimary

Surfaces & backgrounds

Screen background: MaterialTheme.colorScheme.background (your AppColors.Bg)

Cards: MaterialTheme.colorScheme.surface (your AppColors.Surface)

Secondary surfaces: MaterialTheme.colorScheme.surfaceVariant

Borders

Use MaterialTheme.colorScheme.outline (your AppColors.Border)

Discounts / success / danger

Prefer your semantic tokens:

Savings: AppColors.Green600 / AppColors.GreenTextDark

Discount: AppColors.Red600

Error: AppColors.Danger

✅ If you need a new semantic color, add it to AppColors as val, with a clear name.
❌ Don’t add var tokens (see note below).

3) Typography rules (your Poppins + AppTypography)

Use only:

MaterialTheme.typography.displaySmall

headlineSmall

titleLarge, titleMedium

bodyLarge, bodyMedium

labelLarge

✅ If you need a new style, add it to AppTypography and reuse everywhere.
❌ Don’t use TextStyle(fontSize = …) inline in random screens.

4) Component reuse ladder (do this in order)

Existing Omiri components (your ui/components/*)

Existing screen section patterns (copy structure, not styling code)

Material3 primitives (Button, Card, AssistChip, etc.) wrapped in Omiri components

Only then create new component(s)

If a component is “almost right”:

Add a param (variant, tone, leadingIcon, isSelected) instead of duplicating.

5) How to write composables (consistency rules)
Function signature order

Use this order (unless your repo already has another standard):

@Composable
fun ComponentName(
    modifier: Modifier = Modifier,
    // required data
    title: String,
    // optional config
    enabled: Boolean = true,
    // events last
    onClick: () -> Unit = {}
)

Modifier rules

Screens: apply background → padding at the top-level container.

Avoid “modifier soup” in deeply nested children; extract a small section composable.

State rules

Prefer state hoisting:

Screen gets uiState + events from ViewModel

Components get immutable props + callbacks

Don’t introduce a new architecture style if you already have one.

6) Adding new UI tokens (only if missing)

If you notice repeated padding(16.dp) / 12.dp / etc and you don’t already have a spacing system:

Add one file like ui/theme/Dimens.kt (or Spacing.kt) and reuse it.

Same for elevations if needed.

(Only do this if the codebase doesn’t already have an established spacing approach.)

7) File hygiene rules (important for Kotlin)

What you pasted looks like multiple files merged—just keep these rules:

One package … per file

Imports must be at the top (no mid-file imports)

AppColors tokens should be val, not var

Specific note: you currently have:

var SubTextGrey = Color(0xFF6C7280)


Make it:

val SubTextGrey = Color(0xFF6C7280)


(Theme tokens should not be mutable.)

8) “You run / I fix” debug protocol

When something fails, paste:

The full Gradle error (first occurrence + stack trace)

File + line number

The composable you edited

Command you ran (./gradlew assembleDebug, :app:installDebug, etc.)

I’ll respond with minimal changes that preserve your layout/design system.