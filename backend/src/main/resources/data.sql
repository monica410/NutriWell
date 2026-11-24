-- Goal streaks count consecutive completed days with at least one active goal.
-- Re-running this seed does not create duplicate badges.
INSERT IGNORE INTO Badges (name, type, number)
VALUES
    ('1-Day Any Goal Streak', 'ANY_GOAL_STREAK', 1),
    ('3-Day Any Goal Streak', 'ANY_GOAL_STREAK', 3),
    ('7-Day Any Goal Streak', 'ANY_GOAL_STREAK', 7),
    ('30-Day Any Goal Streak', 'ANY_GOAL_STREAK', 30),
    ('100-Day Any Goal Streak', 'ANY_GOAL_STREAK', 100),
    ('365-Day Any Goal Streak', 'ANY_GOAL_STREAK', 365),

    ('1-Day All Goals Streak', 'ALL_GOALS_STREAK', 1),
    ('3-Day All Goals Streak', 'ALL_GOALS_STREAK', 3),
    ('7-Day All Goals Streak', 'ALL_GOALS_STREAK', 7),
    ('30-Day All Goals Streak', 'ALL_GOALS_STREAK', 30),
    ('100-Day All Goals Streak', 'ALL_GOALS_STREAK', 100),
    ('365-Day All Goals Streak', 'ALL_GOALS_STREAK', 365);
