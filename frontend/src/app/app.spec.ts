import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('crée l’application', () => {
    const fixture = TestBed.createComponent(App);

    expect(fixture.componentInstance).toBeTruthy();
  });

  it('affiche le logo DevBrik et la navigation principale', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const racine = fixture.nativeElement as HTMLElement;

    expect(racine.querySelector('.logo')?.textContent).toContain('DevBrik');
    const liens = Array.from(racine.querySelectorAll('nav a')).map((a) => a.textContent?.trim());
    expect(liens).toContain('Briques');
    expect(liens).toContain('Demandes');
  });

  it('visiteur : propose « Se connecter » et « S\'inscrire »', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const texte = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(texte).toContain('Se connecter');
    expect(texte).toContain('S\'inscrire');
  });
});
