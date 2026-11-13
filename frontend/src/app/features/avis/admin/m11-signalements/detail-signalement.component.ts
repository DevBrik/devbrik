import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActionTraitement, libelleMotif, nomAuteur, Signalement } from '../../avis.modeles';

/** Contenu du panneau « Détails du signalement » (le titre de section est dans la page). */
@Component({
  selector: 'avis-detail-signalement',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  templateUrl: './detail-signalement.component.html',
  styleUrl: './detail-signalement.component.scss',
})
export class DetailSignalementComponent {
  readonly signalement = input.required<Signalement>();
  /** Action en cours d'envoi : désactive les deux boutons et change le libellé du bouton cliqué. */
  readonly enCours = input<ActionTraitement | null>(null);
  readonly conserver = output<void>();
  readonly masquer = output<void>();

  protected readonly libelleMotif = libelleMotif;
  protected readonly nomAuteur = nomAuteur;
  protected readonly titreBrique = computed(() => this.signalement().brique?.titre ?? 'Brique indisponible');
}
