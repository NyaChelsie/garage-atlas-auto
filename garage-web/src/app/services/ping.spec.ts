// TestBed = l'outil d'Angular pour preparer un petit environnement de test.
import { TestBed } from '@angular/core/testing';

// On importe le service a tester.
import { Ping } from './ping';

// describe = un groupe de tests portant sur le service Ping.
describe('Ping', () => {
  // Variable qui contiendra le service pendant les tests.
  let service: Ping;

  // beforeEach = ce bloc s'execute avant chaque test.
  beforeEach(() => {
    // On prepare l'environnement de test, puis on demande le service.
    TestBed.configureTestingModule({});
    service = TestBed.inject(Ping);
  });

  // it = un test. Celui-ci verifie simplement que le service existe.
  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
