package de.febanhd.anticrash.checks;

import de.febanhd.anticrash.player.FACPlayer;

public interface ICheck {

    String getName();

    void registerFACPlayer(FACPlayer facPlayer);
}
