import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { formaterNote } from '../../core/libelles';

/** Étoile + « 4,8 sur 5 » (charte 6.4) ; « Pas encore d'avis » sans note. */
@Component({
  selector: 'db-note',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './note.component.html',
  styleUrl: './note.component.scss',
})
export class NoteComponent {
  readonly valeur = input.required<number | null>();
  readonly nombre = input<number>();

  protected readonly texte = computed(() => {
    const v = this.valeur();
    return v === null ? null : formaterNote(v);
  });
}
