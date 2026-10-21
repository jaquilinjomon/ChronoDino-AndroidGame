package com.example.chronodino.model;

public enum FoodItem {
    APPLE("Apple", "🍎", 10, "+10 PTS", 0xFFFEE2E2, 0xFFEF4444),
    MEAT("Meat", "🍖", 20, "+20 PTS", 0xFFFFEDD5, 0xFFEA580C),
    CARROT("Carrot", "🥕", 15, "+15 PTS", 0xFFFEF3C7, 0xFFD97706);

    private final String displayName;
    private final String emoji;
    private final int points;
    private final String pointsLabel;
    private final int backgroundColor;
    private final int accentColor;

    FoodItem(String displayName, String emoji, int points, String pointsLabel, int backgroundColor, int accentColor) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.points = points;
        this.pointsLabel = pointsLabel;
        this.backgroundColor = backgroundColor;
        this.accentColor = accentColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getPoints() {
        return points;
    }

    public String getPointsLabel() {
        return pointsLabel;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getAccentColor() {
        return accentColor;
    }
}
