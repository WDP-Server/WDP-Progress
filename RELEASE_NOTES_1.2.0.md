# 🎉 WDP Progress v1.2.0 - Layered Menu System

A major feature release introducing a complete GUI overhaul with navigable detail menus, 30+ statistics tracking, and comprehensive advancement viewing.

## 🚀 What's New

### Layered Menu System
Complete redesign of the GUI with 5 detailed, scrollable sub-menus:

#### 📖 Advancements Detail Menu
- View **all** Minecraft advancements (100+ items)
- Color-coded completion status (✓ Green = Complete, ✗ Gray = Incomplete)
- Progress tracking and category organization
- Pagination support (28 items per page)

#### ⚔️ Equipment Detail Menu
- **Detailed gear inspection** showing armor, weapons, and notable items
- Displays: Material tier, durability %, enchantments (with levels), estimated value
- Full armor set bonus indicator (+15%)
- Automatic highlighting of special items (Elytra, Trident, Netherite gear)

#### 📊 Statistics Detail Menu
- **30+ gameplay statistics** organized by category
- Movement, Combat, Mining, Building, Interaction stats
- Boss kill tracking (Ender Dragon, Wither, Warden, Elder Guardian)
- Smart formatting with units (km, hearts, hours)

#### 💰 Economy & Experience Detail Menu
- **Combined view** of wealth and XP systems
- Wealth tiers: Broke → Millionaire ($0 to $500K+)
- Level tiers: Novice → Legendary (0 to 50+)
- Milestone tracking and progress visualization

#### ☠️ Death Penalty Detail Menu
- Death statistics and penalty explanation
- **GravesX integration** status and active grave tracking
- Educational content on how penalties work
- Tips for reducing death penalties

### Navigation Features
- **Click categories** in main menu to open detail views
- Previous/Next page buttons for scrolling
- Back button to return to main menu
- Consistent design across all menus

### Admin Features
- New command: `/progressadmin advancements <player>`
- Filter advancements by status or category
- Individual toggle and bulk operations

## 🔧 Technical Improvements

- **Base Architecture**: New `DetailMenu.java` abstract class for all sub-menus
- **Standardized Pagination**: 28 items per page across all menus
- **Menu Detection**: Title-based menu type identification
- **~1,500 lines** of new code across 6 new classes

## 🐛 Bug Fixes

- Fixed GravesX plugin detection (was checking "Graves", now "GravesX")
- Removed non-existent `EAT_CAKE_SLICE` statistic for 1.21.6 compatibility
- Fixed menu instance management

## 📋 Requirements

- **Minecraft**: 1.21.6
- **Java**: 21+
- **Server**: Spigot/Paper
- **Dependencies** (Optional): Vault, GravesX

## 📥 Installation

1. Download `WDPProgress-1.2.0.jar` from this release
2. Place in your server's `plugins/` folder
3. Restart your server
4. Configure in `plugins/WDPProgress/config.yml` if needed

## 📚 Documentation

- [README](https://github.com/wdp-2010/WDP-Progress/blob/main/README.md)
- [Full Changelog](https://github.com/wdp-2010/WDP-Progress/blob/main/UPDATE_LOG_1.2.0.md)
- [GUI Layout Guide](https://github.com/wdp-2010/WDP-Progress/blob/main/GUI_LAYOUT.md)
- [Quick Start](https://github.com/wdp-2010/WDP-Progress/blob/main/QUICK_START.md)

## 🔗 Links

- **GitHub Repository**: https://github.com/wdp-2010/WDP-Progress
- **Issue Tracker**: https://github.com/wdp-2010/WDP-Progress/issues

---

**Full Changelog**: [v1.1.0...v1.2.0](https://github.com/wdp-2010/WDP-Progress/compare/v1.1.0...v1.2.0)

**Made with ❤️ for the WDP Server community**
