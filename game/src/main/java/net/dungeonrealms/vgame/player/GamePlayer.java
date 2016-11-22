package net.dungeonrealms.vgame.player;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.backend.player.DataPlayer;
import net.dungeonrealms.vgame.goal.achievement.EnumAchievement;
import net.dungeonrealms.vgame.goal.objective.Objective;
import net.dungeonrealms.vgame.player.scoreboard.EnumScoreboardType;
import net.dungeonrealms.vgame.player.scoreboard.GameScoreboard;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GamePlayer implements IPlayer
{
    @Getter
    private DataPlayer data; // All raw data

    @Getter
    private Player player;

    @Setter
    @Getter
    private Objective currentObjective;

    @Getter
    @Setter
    private GameScoreboard gameScoreboard;

    public GamePlayer(DataPlayer dataPlayer)
    {
        this.data = dataPlayer;
        this.player = dataPlayer.getPlayer();
    }

    public boolean hasAchievement(EnumAchievement achievement)
    {
        return this.data.getCollectionData().getAchievements().contains(achievement.name());
    }

    public void updateScoreboard(EnumScoreboardType to)
    {
        this.gameScoreboard = new GameScoreboard(to);
        this.gameScoreboard.getScoreboardHolder().send(this.player);
    }
}