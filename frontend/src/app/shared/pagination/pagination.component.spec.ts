import { ComponentRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PaginationComponent } from './pagination.component';

describe('PaginationComponent (charte 6.10)', () => {
  function monter(page: number, totalPages: number) {
    const fixture = TestBed.createComponent(PaginationComponent);
    const ref: ComponentRef<PaginationComponent> = fixture.componentRef;
    ref.setInput('page', page);
    ref.setInput('totalPages', totalPages);
    fixture.detectChanges();
    return { fixture, racine: fixture.nativeElement as HTMLElement };
  }

  it('rien à afficher pour une seule page', () => {
    expect(monter(0, 1).racine.querySelector('nav')).toBeNull();
  });

  it('numéros avec points de suspension (1 2 3 4 5 … 10)', () => {
    const { racine } = monter(0, 10);
    const numeros = Array.from(racine.querySelectorAll('.numero')).map((b) => b.textContent?.trim());
    expect(numeros).toEqual(['1', '2', '3', '4', '5', '10']);
    expect(racine.querySelector('.ellipse')).not.toBeNull();
    expect(racine.querySelector('[aria-current="page"]')?.textContent?.trim()).toBe('1');
  });

  it('« Précédent » désactivé en première page ; « Suivant » émet la page suivante', () => {
    const { fixture, racine } = monter(0, 3);
    const emis: number[] = [];
    fixture.componentInstance.pageChange.subscribe((p) => emis.push(p));
    const [precedent, suivant] = Array.from(racine.querySelectorAll<HTMLButtonElement>('.pas'));
    expect(precedent.disabled).toBe(true);
    suivant.click();
    expect(emis).toEqual([1]);
  });

  it('libellé mobile « Page 2 sur 3 »', () => {
    expect(monter(1, 3).racine.querySelector('.position')?.textContent).toContain('Page 2 sur 3');
  });
});
