import { ChangeDetectionStrategy, Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/** Champ de mot de passe avec bouton œil (charte 6.3), icône cadenas facultative. */
@Component({
  selector: 'db-champ-mot-de-passe',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => ChampMotDePasseComponent), multi: true }],
  templateUrl: './champ-mot-de-passe.component.html',
  styleUrl: './champ-mot-de-passe.component.scss',
})
export class ChampMotDePasseComponent implements ControlValueAccessor {
  readonly identifiant = input.required<string>();
  readonly autocomplete = input<'current-password' | 'new-password'>('current-password');
  readonly placeholder = input('');
  readonly decritPar = input<string | null>(null);
  readonly invalide = input(false);
  readonly avecIcone = input(false);

  protected readonly valeur = signal('');
  protected readonly visible = signal(false);
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
}
