import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    try {
      localStorage.clear();
    } catch {
      // stockage indisponible
    }
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('crée l’application', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('affiche le mot-symbole DevBrik et la navigation Briques · Demandes · Classement', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const racine = fixture.nativeElement as HTMLElement;

    expect(racine.querySelector('.lien-logo')?.textContent).toContain('DevBrik');
    const liens = Array.from(racine.querySelectorAll('.navigation a')).map((a) => a.textContent?.trim());
    expect(liens).toEqual(['Briques', 'Demandes', 'Classement']);
  });

  it('visiteur : propose « Se connecter » et « S\'inscrire »', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const texte = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texte).toContain('Se connecter');
    expect(texte).toContain('S\'inscrire');
  });

  it('a un lien « Aller au contenu » en premier', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const premier = (fixture.nativeElement as HTMLElement).querySelector('a');
    expect(premier?.getAttribute('href')).toBe('#contenu');
  });
});
