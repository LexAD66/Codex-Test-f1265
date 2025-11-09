package de.f_soft_studio.abookplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Einstiegspunkt der Anwendung, damit Hilt den Abhängigkeitsgraphen aufbauen kann.
 * Die Dokumentation erklärt, warum wir Application erweitern: nur so lässt sich
 * der MediaSessionService zuverlässig mit den Repositorys verbinden.
 */
@HiltAndroidApp
class AbookPlayerApp : Application()
