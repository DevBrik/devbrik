import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DemandeResume } from '../../../core/api/modeles';
import { nomAuteur } from '../../../core/libelles';
import { PastilleComponent } from '../../../shared/pastille/pastille.component';

/** Carte d'une demande (M06). Le titre est mis en majuscules par CSS, jamais en transformant la donnée. */
@Component({
  selector: 'db-demande-carte',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, PastilleComponent],
  templateUrl: './demande-carte.component.html',
  styleUrl: './demande-carte.component.scss',
})
export class DemandeCarteComponent {
  readonly demande = input.required<DemandeResume>();
  protected readonly auteur = computed(() => nomAuteur(this.demande().auteur));
  protected readonly ligne = computed(() => {
    const d = this.demande();
    return [this.auteur(), d.categorie?.nom, d.langage?.nom].filter(Boolean).join(' · ');
  });
}
