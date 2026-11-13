import { ChangeDetectionStrategy, Component, ElementRef, forwardRef, inject, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * Saisie par lignes (charte 6.3) : « Entrer pour ajouter » ; chaque Entrée ajoute une pastille.
 * Une pastille se supprime par son bouton ou par Retour arrière dans le champ vide.
 * Valeur : tableau de textes ; l'écran le convertit en texte (une ligne par élément).
 */
@Component({
  selector: 'db-champ-lignes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ChampLignesComponent), multi: true }],
  templateUrl: './champ-lignes.component.html',
  styleUrl: './champ-lignes.component.scss',
})
export class ChampLignesComponent implements ControlValueAccessor {
  private readonly hote = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly identifiant = input('champ-lignes');
  readonly numerote = input(false);
  readonly decritPar = input<string | null>(null);
  readonly invalide = input(false);
  readonly libelleElement = input('élément');

  protected readonly lignes = signal<string[]>([]);
  protected readonly saisie = signal('');
  protected readonly desactive = signal(false);
  protected readonly annonce = signal('');
  private surChangement: (v: string[]) => void = () => undefined;
  protected surTouche: () => void = () => undefined;

  writeValue(v: string[] | null): void {
    this.lignes.set(Array.isArray(v) ? [...v] : []);
  }
  registerOnChange(fn: (v: string[]) => void): void {
    this.surChangement = fn;
  }
  registerOnTouched(fn: () => void): void {
    this.surTouche = fn;
  }
  setDisabledState(d: boolean): void {
    this.desactive.set(d);
  }

  protected clavier(e: KeyboardEvent): void {
    if (e.key === 'Enter') {
      e.preventDefault();
      this.ajouter();
    } else if (e.key === 'Backspace' && this.saisie() === '' && this.lignes().length > 0) {
      e.preventDefault();
      this.retirer(this.lignes().length - 1);
    }
  }

  protected ajouter(): void {
    const texte = this.saisie().replace(/\s+/g, ' ').trim();
    if (!texte) {
      return;
    }
    this.publier([...this.lignes(), texte]);
    this.saisie.set('');
    this.annonce.set(`${this.libelleElement()} ajouté : ${texte}`);
  }

  protected retirer(index: number): void {
    const retire = this.lignes()[index];
    this.publier(this.lignes().filter((_, i) => i !== index));
    this.annonce.set(`${this.libelleElement()} retiré : ${retire}`);
    this.hote.nativeElement.querySelector<HTMLInputElement>('input')?.focus();
  }

  private publier(v: string[]): void {
    this.lignes.set(v);
    this.surChangement(v);
    this.surTouche();
  }
}
