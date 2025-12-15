---
trigger: always_on
---

Omiri Compose UI Style Guide (Component-Only Additions)
1) Hard rule: “No new UI in screens”

If we add anything new visually (a row, card, badge, section, dialog layout, etc.), it must be created as a reusable component under ui/components.

Screens should only:

compose existing components

wire state/events

do minimal layout scaffolding (screen padding, scroll container)

Screens must NOT:

create new “one-off” card layouts inline

hardcode colors/typography/styles inline

duplicate component styling that already exists elsewhere

2) Naming rules (match your repo)

Your folder mixes feature names (DealCard.kt) and “Omiri” prefixed base components (OmiriHeader.kt, OmiriEmptyState.kt). Keep that pattern:

Use these naming conventions

Generic reusable base components: OmiriXyz…

Example: OmiriSectionHeader.kt, OmiriTag.kt, OmiriSurfaceCard.kt

Feature reusable components: descriptive without prefix

Example: BiggestSaversSection.kt, SavingsBreakdownCard.kt

File rule

1 main composable per file once it grows past ~120 lines.

Split helpers into private functions inside the same file unless reused elsewhere.

3) Reuse ladder (what to use first, based on your files)
Header / top bars

Fixed header: OmiriHeader.kt (preferred global)

Home-specific: HomeHeader.kt

Context selection mode: ContextualSelectionTopBar.kt

Generic header: ScreenHeader.kt, SectionHeader.kt

Search

Home: HomeSearchBar.kt

Generic: SearchBar.kt

Chips / filters

Core: FilterChipsRow.kt

Mixed: MixedFilterChipsRow.kt

Categories: CategoryPillsRow.kt

Stores row: StoresSwipeFilterRow.kt

Modal: FilterModal.kt

Cards / content blocks

Deals: DealCard.kt, DealOfTheDay.kt, DealsCarousel.kt, DealsGrid.kt, TrendingDealsSection.kt, FeaturedDealsRow.kt

Summaries: OmiriSummaryCard.kt, StatsCards.kt, SavingsDashboard.kt

Tips/empty/loading: OmiriTipCard.kt, OmiriEmptyState.kt, OmiriSmartEmptyState.kt, OmiriLoader.kt, Shimmer.kt, DealCardSkeleton.kt

Alerts/AI: NotificationCard.kt, SmartAlertsCard.kt, AiChatFunctionCards.kt, ShoppingListChatCard.kt

Lists / shopping

List section: ShoppingListsSection.kt

Item row: ShoppingListItem.kt

Create/manage: CreateListBottomSheet.kt, ManageListsDialog.kt, ListSelectionBottomSheet.kt, AddItemDialog.kt, AddItemBottomSheet.kt

Membership cards

Add: AddMembershipCardBottomSheet.kt

Details: MembershipCardDetailsBottomSheet.kt

Store selection / location

StoreComponents.kt, StoreSelectionComponents.kt, StoreLocationModal.kt

✅ Rule: if your new UI resembles anything above, extend that component via params instead of creating a duplicate.

4) “New element = new reusable component” definition

A “new element” includes (examples):

a new section (“Biggest Savers”)

a new card layout

a new badge/tag style

a new row layout (e.g., store plan row)

a new empty state variant

a new bottom sheet layout

a new skeleton/loading pattern

All of these must be components in ui/components.

5) Component API rules (so they stay reusable)

Every new component must:

accept modifier: Modifier = Modifier

accept data in via params (immutable)

expose events out via lambdas (onClick, onDismiss, onSelect, etc.)

rely on MaterialTheme + your AppColors tokens (no new hex in feature code)

Preferred param order
@Composable
fun BiggestSaversSection(
    modifier: Modifier = Modifier,
    title: String,
    items: List<BiggestSaverUi>,
    onItemClick: (BiggestSaverUi) -> Unit,
    onSeeAllClick: () -> Unit,
)

6) How we add something like “Biggest Savers” correctly

If you want a new “Biggest Savers” block on Home:

✅ Create:

BiggestSaversSection.kt (reusable section)

optionally BiggestSaverCard.kt if a new card layout is needed
(if it can reuse DealCard/OmiriSummaryCard, don’t create a new card)

✅ In the screen:

only call BiggestSaversSection(...) and wire state

7) Implementation constraints (design-locked)

Do not change existing paddings/radius/elevation unless asked.

Use MaterialTheme.typography.* (your Poppins setup) — no inline TextStyle(...) unless adding a shared token.

Don’t introduce new colors in screens. Add to AppColors if truly needed (as val, not var).

8) “You run / I fix” rule

I will never ask you to trust that it compiles. You run it and paste:

the full error

file + line

the command you ran

I respond with the smallest possible patch.


8) Build & run command rule (mandatory)

Always use Gradle Wrapper and assume it exists.

Never ask which command to run. Default to wrapper commands.

Default commands to suggest

Build: ./gradlew build

Debug install: ./gradlew :app:installDebug

Unit tests: ./gradlew test

Instrumented tests: ./gradlew connectedDebugAndroidTest

Lint: ./gradlew lint

Clean rebuild (only if needed): ./gradlew clean build