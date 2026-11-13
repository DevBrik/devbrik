import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BriqueResume } from '../../core/api/modeles';
import { libelleLicence, nomAuteur, pastilleTest } from '../../core/libelles';
import { LangageBadgeComponent } from '../langage-badge/langage-badge.component';
import { NoteComponent } from '../note/note.component';
import { PastilleComponent, TypePastille } from '../pastille/pastille.component';

/** Mode des actions : aucune (liste publique), auteur (Mes briques), republier (administration M11). */
export type ModeActionsCarte = 'aucune' | 'auteur' | 'republier';

/** Carte de brique (charte 6.5). Aucune action n'apparaît au survol : tout est visible en permanence. */
@Component({
  selector: 'db-brique-carte',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, LangageBadgeComponent, PastilleComponent, NoteComponent],
  templateUrl: './brique-carte.component.html',
  styleUrl: './brique-carte.component.scss',
})
export class BriqueCarteComponent {
  readonly brique = input.required<BriqueResume>();
  readonly actions = input<ModeActionsCarte>('aucune');
  readonly enCours = input(false);
  readonly modifier = output<void>();
  readonly supprimer = output<void>();
  readonly republier = output<void>();

  protected readonly test = computed(() => pastilleTest(this.brique()));
  protected readonly typeTest = computed<TypePastille>(() => {
    const t = this.test();
    if (t.classe === 'ok') {
      return 'test-ok';
    }
    if (t.classe === 'ko') {
      return 'test-ko';
    }
    return t.libelle === 'Avis partagés' ? 'avis-partages' : 'non-testee';
  });
  protected readonly licence = computed(() => libelleLicence(this.brique().licence));
  protected readonly auteur = computed(() => nomAuteur(this.brique().auteur));
}
