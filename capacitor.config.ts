import type { CapacitorConfig } from '@capacitor/cli';

// Fusion Capacitor (Phase 3, Lot 3A) : enveloppe le site clovis-frontend
// existant (chat, bibliotheque, identite "Nuit d'etude") au lieu de
// reconstruire une app a part. webDir pointe vers l'export statique du
// submodule web/ (branche capacitor-export, voir web/next.config.mjs).
const config: CapacitorConfig = {
  appId: 'ai.djiguigne.clovis',
  appName: 'Clovis',
  webDir: 'web/out',
  backgroundColor: '#0F0D0B',
};

export default config;
