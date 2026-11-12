import { TestBed } from '@angular/core/testing';
import { requis } from '../avis.testing';
import { PaginationLocaleComponent } from './pagination-locale.component';

function monter(page: number, totalPages: number) {
  TestBed.configureTestingModule({ imports: [PaginationLocaleComponent] });
  const fixture = TestBed.createComponent(PaginationLocaleComponent);
  fixture.componentRef.setInput('page', page);
  fixture.componentRef.setInput('totalPages', totalPages);
  fixture.componentRef.setInput('libelle', 'Pagination des signalements');
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  const pages: number[] = [];
  fixture.componentInstance.pageChange.subscribe((p) => pages.push(p));
  const bouton = (texte: string) =>
    requis(Array.from(racine.querySelectorAll('button')).find((b) => b.textContent?.trim() === texte));
  return { racine, pages, bouton };
}

describe('PaginationLocaleComponent', () => {
  it('une seule page : rien n’est affiché', () => {
    const { racine } = monter(0, 1);

    expect(racine.querySelector('nav')).toBeNull();
  });

  it('affiche « Page 1 sur 3 » dans une navigation nommée', () => {
    const { racine } = monter(0, 3);

    expect(racine.textContent).toContain('Page 1 sur 3');
    expect(racine.querySelector('nav')?.getAttribute('aria-label')).toBe('Pagination des signalements');
  });

  it('première page : « Précédent » est désactivé et ne fait rien', () => {
    const { bouton, pages } = monter(0, 3);

    expect(bouton('← Précédent').disabled).toBe(true);
    bouton('← Précédent').click();
    expect(pages).toEqual([]);
  });

  it('page du milieu : Précédent et Suivant émettent la page voisine (à partir de 0)', () => {
    const { bouton, pages } = monter(1, 3);

    bouton('← Précédent').click();
    bouton('Suivant →').click();

    expect(pages).toEqual([0, 2]);
  });

  it('dernière page : « Suivant » est désactivé', () => {
    const { bouton } = monter(2, 3);

    expect(bouton('Suivant →').disabled).toBe(true);
  });
});
