import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ChampNoteLocalComponent } from './champ-note-local.component';

@Component({
  imports: [ReactiveFormsModule, ChampNoteLocalComponent],
  template: `<avis-champ-note [formControl]="note" />`,
})
class HoteComponent {
  readonly note = new FormControl<number | null>(null);
}

describe('ChampNoteLocalComponent', () => {
  function monter() {
    const fixture = TestBed.createComponent(HoteComponent);
    fixture.detectChanges();
    const racine = fixture.nativeElement as HTMLElement;
    const boutons = () => Array.from(racine.querySelectorAll<HTMLButtonElement>('button[role="radio"]'));
    const toucher = (indice: number, touche: string) => {
      boutons()[indice].dispatchEvent(new KeyboardEvent('keydown', { key: touche, bubbles: true }));
      fixture.detectChanges();
    };
    return { fixture, racine, boutons, toucher };
  }

  it('affiche cinq étoiles avec un texte accessible « n sur 5 »', () => {
    const { boutons } = monter();
    expect(boutons().map((b) => b.getAttribute('aria-label'))).toEqual([
      '1 sur 5', '2 sur 5', '3 sur 5', '4 sur 5', '5 sur 5',
    ]);
  });

  it('une seule étoile est dans l’ordre de tabulation : la première, puis la note choisie', () => {
    const { fixture, boutons } = monter();
    expect(boutons().map((b) => b.getAttribute('tabindex'))).toEqual(['0', '-1', '-1', '-1', '-1']);

    boutons()[2].click();
    fixture.detectChanges();

    expect(boutons().map((b) => b.getAttribute('tabindex'))).toEqual(['-1', '-1', '0', '-1', '-1']);
    expect(boutons()[2].getAttribute('aria-checked')).toBe('true');
  });

  it('les flèches changent la note et déplacent le focus', () => {
    const { fixture, boutons, toucher } = monter();
    const hote = fixture.componentInstance;
    boutons()[2].click();
    fixture.detectChanges();

    toucher(2, 'ArrowRight');
    expect(hote.note.value).toBe(4);
    expect(document.activeElement).toBe(boutons()[3]);

    toucher(3, 'Home');
    expect(hote.note.value).toBe(1);

    toucher(0, 'End');
    expect(hote.note.value).toBe(5);

    toucher(4, 'ArrowRight');
    expect(hote.note.value).toBe(5);
  });
});
