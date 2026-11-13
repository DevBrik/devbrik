import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Membre {
  numero: number;
  role: string;
  module: string;
}

/** M13 : à propos (/infos/a-propos) : objectif, communauté, équipe. */
@Component({
  selector: 'db-a-propos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './a-propos.component.html',
  styleUrl: './a-propos.component.scss',
})
export class AProposComponent {
  protected readonly equipe: Membre[] = [
    { numero: 1, role: 'Lead technique', module: 'Architecture, sécurité, déploiement et maquettes' },
    { numero: 2, role: 'Briques', module: 'Catalogue, recherche, publication et modération des briques' },
    { numero: 3, role: 'Confiance et modération', module: 'Avis, tests, signalements et statistiques' },
    { numero: 4, role: 'Échange', module: 'Demandes, réponses, profils et réputation' },
    { numero: 5, role: 'Comptes et conformité', module: 'Inscription, connexion, compte et pages légales' },
  ];
}
