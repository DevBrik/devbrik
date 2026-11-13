import { ChangeDetectionStrategy, Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/** Zone de code éditable (charte 6.7) : monospace, en-tête « Code source », compteur (30 000 au plus). */
@Component({
  selector: 'db-champ-code',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ChampCodeComponent), multi: true }],
  templateUrl: './champ-code.component.html',
  styleUrl: './champ-code.component.scss',
})
export class ChampCodeComponent implements ControlValueAccessor {
  readonly identifiant = input('champ-code');
  readonly max = input(30000);
  readonly decritPar = input<string | null>(null);
  readonly invalide = input(false);

  protected readonly valeur = signal('');
  protected readonly desactive = signal(false);
  private surChangement: (v: string) => void = () => undefined;
  protected surTouche: () => void = () => undefined;

  writeValue(v: string | null): void {
    this.valeur.set(v ?? '');
  }
  registerOnChange(fn: (v: string) => void): void {
    this.surChangement = fn;
  }
  registerOnTouched(fn: () => void): void {
    this.surTouche = fn;
  }
  setDisabledState(d: boolean): void {
    this.desactive.set(d);
  }

  protected saisir(v: string): void {
    this.valeur.set(v);
    this.surChangement(v);
  }

  /** Tabulation : insère deux espaces ; Échap puis Tab permet de quitter la zone au clavier. */
  private echap = false;
  protected clavier(e: KeyboardEvent): void {
    if (e.key === 'Escape') {
      this.echap = true;
      return;
    }
    if (e.key === 'Tab' && !e.shiftKey && !this.echap) {
      e.preventDefault();
      const zone = e.target as HTMLTextAreaElement;
      const debut = zone.selectionStart;
      const fin = zone.selectionEnd;
      const v = zone.value.slice(0, debut) + '  ' + zone.value.slice(fin);
      zone.value = v;
      zone.selectionStart = zone.selectionEnd = debut + 2;
      this.saisir(v);
      return;
    }
    this.echap = false;
  }
}
