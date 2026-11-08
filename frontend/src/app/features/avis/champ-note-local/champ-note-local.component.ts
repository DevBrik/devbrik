import { ChangeDetectionStrategy, Component, ElementRef, forwardRef, inject, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { STYLES_COMMUNS } from '../avis.styles';

/** Choix d'une note de 1 à 5 : groupe radio, flèches, Début / Fin, texte « n sur 5 ». */
@Component({
  selector: 'avis-champ-note',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ChampNoteLocalComponent), multi: true },
  ],
  template: `
    <div
      class="groupe"
      role="radiogroup"
      [attr.aria-labelledby]="libelleId()"
      [attr.aria-describedby]="decritPar()"
      [attr.aria-invalid]="invalide() ? 'true' : null"
      (focusout)="toucher()"
    >
      @for (n of valeurs; track n) {
        <button
          type="button"
          role="radio"
          class="etoile"
          [disabled]="desactive()"
          [attr.aria-checked]="valeur() === n"
          [attr.aria-label]="n + ' sur 5'"
          [attr.tabindex]="indexFocus(n)"
          [attr.data-invalide]="invalide() ? 'true' : null"
          (click)="choisir(n)"
          (keydown)="clavier($event, n)"
        >
          <span aria-hidden="true">{{ (valeur() ?? 0) >= n ? '★' : '☆' }}</span>
        </button>
      }
    </div>
    <p class="texte">{{ valeur() === null ? 'Aucune note choisie' : valeur() + ' sur 5' }}</p>
  `,
  styles: [
    STYLES_COMMUNS,
    `
      .groupe { display: flex; gap: 0.25rem; }
      .etoile {
        min-width: 44px; min-height: 44px; padding: 0; border: 0; background: transparent;
        font-size: 2rem; line-height: 1; color: var(--db-orange); cursor: pointer;
      }
      .etoile:disabled { cursor: not-allowed; opacity: 0.55; }
      .texte { margin: 0.25rem 0 0; font-size: 1rem; }
    `,
  ],
})
export class ChampNoteLocalComponent implements ControlValueAccessor {
  readonly libelleId = input<string | null>(null);
  readonly decritPar = input<string | null>(null);
  readonly invalide = input(false);

  protected readonly valeurs = [1, 2, 3, 4, 5];
  protected readonly valeur = signal<number | null>(null);
  protected readonly desactive = signal(false);

  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);
  private surChangement: (v: number | null) => void = () => undefined;
  private surTouche: () => void = () => undefined;

  writeValue(v: number | null): void {
    this.valeur.set(v ?? null);
  }

  registerOnChange(fn: (v: number | null) => void): void {
    this.surChangement = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.surTouche = fn;
  }

  setDisabledState(desactive: boolean): void {
    this.desactive.set(desactive);
  }

  /** Une seule étoile est dans l'ordre de tabulation (la note choisie, sinon la première). */
  protected indexFocus(n: number): number {
    return n === (this.valeur() ?? 1) ? 0 : -1;
  }

  protected choisir(n: number): void {
    this.valeur.set(n);
    this.surChangement(n);
    this.surTouche();
  }

  protected toucher(): void {
    this.surTouche();
  }

  protected clavier(evenement: KeyboardEvent, n: number): void {
    let cible: number;
    switch (evenement.key) {
      case 'ArrowRight':
      case 'ArrowUp':
        cible = Math.min(5, n + 1);
        break;
      case 'ArrowLeft':
      case 'ArrowDown':
        cible = Math.max(1, n - 1);
        break;
      case 'Home':
        cible = 1;
        break;
      case 'End':
        cible = 5;
        break;
      default:
        return;
    }
    evenement.preventDefault();
    this.choisir(cible);
    this.hote.nativeElement.querySelectorAll<HTMLButtonElement>('button')[cible - 1]?.focus();
  }
}
