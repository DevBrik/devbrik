import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AdminEnteteComponent } from './admin-entete.component';

async function monter(url: string) {
  TestBed.configureTestingModule({
    imports: [AdminEnteteComponent],
    providers: [
      provideRouter([
        { path: 'admin/signalements', children: [] },
        { path: 'admin/statistiques', children: [] },
      ]),
    ],
  });
  await TestBed.inject(Router).navigateByUrl(url);
  const fixture = TestBed.createComponent(AdminEnteteComponent);
  fixture.detectChanges();
  await fixture.whenStable();
  fixture.detectChanges();
  return fixture.nativeElement as HTMLElement;
}

describe('AdminEnteteComponent', () => {
  it('affiche le titre « Administration » et les deux onglets', async () => {
    const racine = await monter('/admin/signalements');

    expect(racine.querySelector('h1')?.textContent).toBe('Administration');
    const onglets = Array.from(racine.querySelectorAll('a')).map((a) => a.textContent?.trim());
    expect(onglets).toEqual(['Signalements', 'Statistiques']);
  });

  it('sur /admin/signalements, seul « Signalements » porte aria-current', async () => {
    const racine = await monter('/admin/signalements');

    const [signalements, statistiques] = Array.from(racine.querySelectorAll('a'));
    expect(signalements.getAttribute('aria-current')).toBe('page');
    expect(statistiques.hasAttribute('aria-current')).toBe(false);
  });

  it('sur /admin/statistiques, seul « Statistiques » porte aria-current', async () => {
    const racine = await monter('/admin/statistiques');

    const [signalements, statistiques] = Array.from(racine.querySelectorAll('a'));
    expect(statistiques.getAttribute('aria-current')).toBe('page');
    expect(signalements.hasAttribute('aria-current')).toBe(false);
  });

  it('les onglets sont de vrais liens vers les deux routes', async () => {
    const racine = await monter('/admin/signalements');

    const liens = Array.from(racine.querySelectorAll('a')).map((a) => a.getAttribute('href'));
    expect(liens).toEqual(['/admin/signalements', '/admin/statistiques']);
  });
});
