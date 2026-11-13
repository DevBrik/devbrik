import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

export type TypeEtat = 'chargement' | 'vide' | 'erreur' | '401' | '403' | '404';

const DEFAUTS: Record<TypeEtat, { icone: string; titre: string; message: string }> = {
  chargement: { icone: 'hourglass_empty', titre: 'Chargement en cours…', message: '' },
  vide: { icone: 'inventory_2', titre: 'Rien à afficher pour le moment', message: '' },
  erreur: {
    icone: 'wifi_off',
    titre: 'Connexion impossible',
    message: 'Vérifiez votre connexion Internet puis réessayez.',
  },
  '401': { icone: 'person', titre: 'Connexion requise', message: 'Connectez-vous pour accéder à cette fonctionnalité.' },
  '403': { icone: 'block', titre: 'Accès refusé', message: "Vous n'avez pas l'autorisation d'effectuer cette action." },
  '404': {
    icone: 'web_asset_off',
    titre: 'Page introuvable',
    message: "La ressource demandée n'existe pas ou n'est plus disponible.",
  },
};

/** États transverses (M14, charte 8) : vide, chargement, erreur réseau, 401, 403, 404. */
@Component({
  selector: 'db-etat-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './etat-page.component.html',
  styleUrl: './etat-page.component.scss',
})
export class EtatPageComponent {
  readonly type = input.required<TypeEtat>();
  readonly titre = input<string>();
  readonly message = input<string>();
  /** Bouton facultatif : lien (actionLien) ou événement (action). */
  readonly actionLibelle = input<string>();
  readonly actionLien = input<string | unknown[]>();
  readonly actionParametres = input<Record<string, string>>();
  readonly action = output<void>();
  /** Erreur réseau : bouton « Réessayer ». */
  readonly reessayer = output<void>();

  protected readonly defaut = computed(() => DEFAUTS[this.type()]);
  protected readonly titreAffiche = computed(() => this.titre() ?? this.defaut().titre);
  protected readonly messageAffiche = computed(() => this.message() ?? this.defaut().message);
  protected readonly role = computed(() => (this.type() === 'erreur' ? 'alert' : 'status'));
  /** Libellé par défaut des états qui ont une action utile. */
  protected readonly libelleAction = computed(() => {
    if (this.actionLibelle()) {
      return this.actionLibelle();
    }
    if (this.type() === '404') {
      return "Retour à l'accueil";
    }
    if (this.type() === '401') {
      return 'Se connecter';
    }
    return undefined;
  });
  protected readonly lien = computed(() => {
    if (this.actionLien()) {
      return this.actionLien();
    }
    if (this.type() === '404') {
      return '/briques';
    }
    if (this.type() === '401') {
      return '/compte/connexion';
    }
    return undefined;
  });
}
