package com.cspinventory.util;

import java.util.List;
import java.util.Map;

public final class EmplacementCatalog {

    public static final List<String> SITES = List.of(
            "Tramelan",
            "Bienne",
            "Moutier"
    );

    public static final Map<String, List<String>> LIEUX_PAR_SITE = Map.of(
            "Tramelan", List.of(
                    "AC 2 IpPlus", "AC POIAS", "AC Promenade 3", "Bureau ACF", "Bureau Direction",
                    "Bureau Finances", "Bureau GTI", "Bureau IP", "Bureau IT", "Bureau IpPlus",
                    "Bureau POIAS/SSIP", "Bureau RH", "Bureau magasin", "Bureau vente", "Cafeteria",
                    "Edition OPEC", "Magasin", "Metal", "Reception", "SAD", "Salle A", "Salle B",
                    "Salle D", "Salle F", "Salle G", "Salle J", "Salle K", "Salle M", "Salle N",
                    "Salle O", "Salle P", "Stock principal", "Rack AB03A (T2)", "Rack AB03B (T2)"
            ),
            "Bienne", List.of(
                    "AC POIAS", "AMM TRF & BIAS", "BW 2", "BW 3", "Bureau Direction", "Bureau GTI",
                    "Bureau IT", "Bureau menuiserie/bois", "Bureau 1", "Bureau 2", "Bureau BIN",
                    "Bureau BinPlus", "Bureau SAD", "Bureau FK", "Cafeteria", "Coaching 1", "Coaching 2",
                    "Conseil 1", "Conseil 2", "Coordination 1", "Coordination 2", "Coordination 3",
                    "Coordination", "Fide 1", "Salle A", "Salle C", "Salle F", "Salle G", "Salle H",
                    "Magasin", "POIAS", "dexterite", "Salle N", "SADD", "SADF", "Social et dettes",
                    "Travail 1"
            ),
            "Moutier", List.of(
                    "Bureau Direction", "Conseil 1", "Conseil 2", "Coordination", "Reception", "Social et dettes"
            )
    );

    private EmplacementCatalog() {
    }
}
