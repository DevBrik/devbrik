import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Subject, debounceTime } from 'rxjs';
import { ReferentielService } from '../../core/api/referentiel.service';
import { TRIS } from '../../core/libelles';

export interface Filtres {
  q: string;
  categorie: string;
  langage: string;
  tri: string;
}

export const FILTRES_VIDES: Filtres = { q: '', categorie: '', langage: '', tri: 'recentes' };

/**
 * Recherche + catégorie + langage + tri facultatif (dossier 12.6).
 * La recherche est émise 300 ms après la dernière frappe, quand elle est vide ou fait 2 à 100 caractères (RG16).
 */
@Component({
  selector: 'db-barre-filtres',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './barre-filtres.component.html',
  styleUrl: './barre-filtres.component.scss',
})
export class BarreFiltresComponent implements OnInit {
  private readonly referentiels = inject(ReferentielService);
  private readonly destroyRef = inject(DestroyRef);

  readonly avecTri = input(true);
  readonly placeholder = input('Rechercher une brique');
  /** Valeurs initiales (lues dans l'adresse par l'écran). */
  readonly valeurs = input<Filtres>(FILTRES_VIDES);
  readonly identifiant = input('filtres');
  readonly filtresChange = output<Filtres>();

  protected readonly categories = toSignal(this.referentiels.categories(), { initialValue: [] });
  protected readonly langages = toSignal(this.referentiels.langages(), { initialValue: [] });
  protected readonly tris = TRIS;

  protected readonly q = signal('');
  protected readonly categorie = signal('');
  protected readonly langage = signal('');
  protected readonly tri = signal('recentes');
  protected readonly qTropCourt = computed(() => {
    const t = this.q().trim().length;
    return t === 1;
  });

  private readonly frappe = new Subject<void>();

  constructor() {
    effect(() => {
      const v = this.valeurs();
      this.q.set(v.q);
      this.categorie.set(v.categorie);
      this.langage.set(v.langage);
      this.tri.set(v.tri || 'recentes');
    });
  }

  ngOnInit(): void {
    this.frappe.pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      const t = this.q().trim().length;
      if (t === 0 || (t >= 2 && t <= 100)) {
        this.emettre();
      }
    });
  }

  protected saisir(valeur: string): void {
    this.q.set(valeur);
    this.frappe.next();
  }

  protected changer(champ: 'categorie' | 'langage' | 'tri', valeur: string): void {
    this[champ].set(valeur);
    this.emettre();
  }

  /** Entrée dans le champ de recherche : émission immédiate. */
  protected valider(evenement: Event): void {
    evenement.preventDefault();
    const t = this.q().trim().length;
    if (t === 0 || (t >= 2 && t <= 100)) {
      this.emettre();
    }
  }

  private emettre(): void {
    this.filtresChange.emit({
      q: this.q().trim(),
      categorie: this.categorie(),
      langage: this.langage(),
      tri: this.avecTri() ? this.tri() : '',
    });
  }
}
