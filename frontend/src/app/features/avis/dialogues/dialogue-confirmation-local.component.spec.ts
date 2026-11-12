import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { requis } from '../avis.testing';
import { DialogueConfirmationLocalComponent } from './dialogue-confirmation-local.component';

function monter() {
  const fermetures: unknown[] = [];
  TestBed.configureTestingModule({
    imports: [DialogueConfirmationLocalComponent],
    providers: [
      {
        provide: MAT_DIALOG_DATA,
        useValue: { titre: 'Masquer cette brique ?', message: 'La brique disparaîtra.', libelleConfirmer: 'Masquer la brique' },
      },
      { provide: MatDialogRef, useValue: { close: (v?: unknown) => fermetures.push(v) } },
    ],
  });
  const fixture = TestBed.createComponent(DialogueConfirmationLocalComponent);
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  const bouton = (texte: string) =>
    requis(Array.from(racine.querySelectorAll('button')).find((b) => b.textContent?.trim() === texte));
  return { racine, fermetures, bouton };
}

describe('DialogueConfirmationLocalComponent', () => {
  it('nomme l’objet : titre, message et libellé de confirmation viennent des données', () => {
    const { racine, bouton } = monter();

    expect(racine.querySelector('h2')?.textContent).toContain('Masquer cette brique ?');
    expect(racine.textContent).toContain('La brique disparaîtra.');
    expect(bouton('Masquer la brique')).toBeTruthy();
  });

  it('confirmer ferme avec true', () => {
    const { bouton, fermetures } = monter();

    bouton('Masquer la brique').click();

    expect(fermetures).toEqual([true]);
  });

  it('« Annuler » ferme avec false', () => {
    const { bouton, fermetures } = monter();

    bouton('Annuler').click();

    expect(fermetures).toEqual([false]);
  });

  it('le focus initial est demandé sur « Annuler » (action sûre par défaut)', () => {
    const { bouton } = monter();

    expect(bouton('Annuler').hasAttribute('cdkfocusinitial')).toBe(true);
  });
});
