import { ChangeDetectionStrategy, Component, ElementRef, forwardRef, inject, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/** Choix d'une note de 1 à 5 : groupe radio, flèches, Début / Fin, texte « n sur 5 ». */
@Component({
  selector: 'db-champ-note',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ChampNoteComponent), multi: true },
  ],
  templateUrl: './champ-note.component.html',
  styleUrl: './champ-note.component.scss',
})
export class ChampNoteComponent implements ControlValueAccessor {
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
